package com.audiobook.vc.features.googledrive

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audiobook.vc.core.common.DispatcherProvider
import com.audiobook.vc.core.googledrive.DriveFile
import com.audiobook.vc.core.googledrive.GoogleDriveAuthManager
import com.audiobook.vc.core.googledrive.GoogleDriveClient
import com.audiobook.vc.core.googledrive.GoogleDriveDocumentFile

import com.audiobook.vc.navigation.Navigator
import com.audiobook.vc.core.data.folders.AudiobookFolders
import com.audiobook.vc.navigation.Destination
import com.audiobook.vc.navigation.Origin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.AssistedFactory
import kotlinx.coroutines.launch
import com.audiobook.vc.core.logging.api.Logger


@AssistedInject
class GoogleDriveViewModel(
  private val googleDriveClient: GoogleDriveClient,
  private val googleDriveAuthManager: GoogleDriveAuthManager,
  private val dispatcherProvider: DispatcherProvider,
  private val navigator: Navigator,
  private val audiobookFolders: AudiobookFolders,
  @Assisted private val origin: Origin,
) : ViewModel() {

  var state by mutableStateOf(GoogleDriveViewState())
    private set

  private val breadcrumbs = mutableListOf<Breadcrumb>()
  
  // Cache for previously loaded folders
  private val folderCache = mutableMapOf<String?, List<DriveFile>>()

  init {
    if (!googleDriveClient.isConnected()) {
      state = state.copy(signInRequired = true)
    } else {
      loadFiles(null) // Root
    }
  }

  fun loadFiles(folderId: String?, folderName: String? = null) {
    Logger.d("loadFiles: folderId=$folderId, folderName=$folderName")
    viewModelScope.launch {
      state = state.copy(isLoading = true, error = null)
      
      // Update breadcrumbs
      if (folderId == null) {
        breadcrumbs.clear()
        breadcrumbs.add(Breadcrumb("Root", null))
      } else if (folderName != null) {
        // If navigating down
        if (breadcrumbs.none { it.id == folderId }) {
           breadcrumbs.add(Breadcrumb(folderName, folderId))
        } else {
           // If navigating up/back
           val index = breadcrumbs.indexOfFirst { it.id == folderId }
           if (index != -1 && index < breadcrumbs.size - 1) {
             breadcrumbs.subList(index + 1, breadcrumbs.size).clear()
           }
        }
      }

      try {
        val files = googleDriveClient.listFiles(folderId)
        // Cache the result
        folderCache[folderId] = files
        
        state = state.copy(
          files = files,
          currentFolderId = folderId,
          currentFolderName = folderName ?: "Google Drive",
          breadcrumbs = breadcrumbs.toList(),
          isLoading = false,
          isRefreshing = false
        )
      } catch (e: Exception) {
        Logger.e(e, "Failed to load files")
        state = state.copy(
          isLoading = false,
          isRefreshing = false,
          error = e.message ?: "Failed to load files"
        )
      }
    }
  }

  /**
   * Refresh the current folder contents.
   */
  fun refresh() {
    viewModelScope.launch {
      state = state.copy(isRefreshing = true, error = null)
      loadFiles(state.currentFolderId, state.currentFolderName)
    }
  }

  /**
   * Retry after an error (re-initiates sign-in if needed).
   */
  fun retry() {
    state = state.copy(error = null, signInFailed = false)
    if (!googleDriveClient.isConnected()) {
      state = state.copy(signInRequired = true)
    } else {
      loadFiles(state.currentFolderId, state.currentFolderName)
    }
  }

  fun onSignInResult(data: android.content.Intent?) {
    Logger.d("onSignInResult called")
    viewModelScope.launch {
      googleDriveAuthManager.handleSignInResult(data).fold(
        onSuccess = {
          state = state.copy(signInRequired = false, signInFailed = false, errorDetails = null)
          loadFiles(null)
        },
        onFailure = { e ->
          onSignInFailure(e.message ?: "Unknown error during sign-in")
        }
      )
    }
  }

  fun onSignInFailure(errorMessage: String?) {
    Logger.e("Sign-in failed: $errorMessage")
    state = state.copy(signInRequired = false, signInFailed = true, errorDetails = errorMessage)
  }

  fun getSignInIntent() = googleDriveClient.getSignInIntent()

  fun onFileClick(file: DriveFile) {
    if (file.isFolder) {
      loadFiles(file.id, file.name)
    }
  }
  
  fun onBackClick() {
    if (breadcrumbs.size > 1) {
      val parent = breadcrumbs[breadcrumbs.size - 2]
      
      // Use cached data if available for faster back navigation
      val cachedFiles = folderCache[parent.id]
      if (cachedFiles != null) {
        breadcrumbs.removeAt(breadcrumbs.size - 1)
        state = state.copy(
          files = cachedFiles,
          currentFolderId = parent.id,
          currentFolderName = parent.name,
          breadcrumbs = breadcrumbs.toList()
        )
      } else {
        loadFiles(parent.id, parent.name)
      }
    } else {
      navigator.goBack()
    }
  }

  fun onSelectCurrentFolder() {
    val currentId = state.currentFolderId ?: "root"
    val uri = Uri.Builder()
      .scheme(GoogleDriveDocumentFile.GOOGLE_DRIVE_SCHEME)
      .authority(currentId)
      .build()
    
    Logger.d("onSelectCurrentFolder: uri=$uri")
    navigator.goTo(Destination.SelectFolderType(uri, origin))
  }

  @AssistedFactory
  interface Factory {
    fun create(origin: Origin): GoogleDriveViewModel
  }
}

data class GoogleDriveViewState(
  val files: List<DriveFile> = emptyList(),
  val isLoading: Boolean = false,
  val isRefreshing: Boolean = false,
  val currentFolderId: String? = null,
  val currentFolderName: String = "Google Drive",
  val breadcrumbs: List<Breadcrumb> = emptyList(),
  val signInRequired: Boolean = false,
  val signInFailed: Boolean = false,
  val error: String? = null,
  val errorDetails: String? = null,
)

data class Breadcrumb(val name: String, val id: String?)

