package com.audiobook.vc.features.googledrive

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
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
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.AssistedFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

  init {
    if (!googleDriveClient.isConnected()) {
      state = state.copy(signInRequired = true)
    } else {
      loadFiles(null) // Root
    }
  }

  fun loadFiles(folderId: String?, folderName: String? = null) {
    android.util.Log.d("GoogleDriveViewModel", "loadFiles: folderId=$folderId, folderName=$folderName")
    viewModelScope.launch {
      state = state.copy(isLoading = true)
      
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

      val files = googleDriveClient.listFiles(folderId)
      state = state.copy(
        files = files,
        currentFolderId = folderId,
        currentFolderName = folderName ?: "Google Drive",
        breadcrumbs = breadcrumbs.toList(),
        isLoading = false
      )
    }
  }

  fun onSignInResult(data: android.content.Intent?) {
    Logger.d("GoogleDriveViewModel: onSignInResult called, data=$data")
    viewModelScope.launch {
      val success = googleDriveAuthManager.handleSignInResult(data)
      Logger.d("GoogleDriveViewModel: handleSignInResult returned $success")
      if (success) {
        state = state.copy(signInRequired = false)
        loadFiles(null)
      } else {
        Logger.e("GoogleDriveViewModel: Sign-in failed")
        // Stay on screen but clear signInRequired to prevent re-launch loop
        state = state.copy(signInRequired = false, signInFailed = true)
      }
    }
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
        loadFiles(parent.id, parent.name) 
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
    
    android.util.Log.d("GoogleDriveViewModel", "onSelectCurrentFolder: uri=$uri")
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
  val currentFolderId: String? = null,
  val currentFolderName: String = "Google Drive",
  val breadcrumbs: List<Breadcrumb> = emptyList(),
  val signInRequired: Boolean = false,
  val signInFailed: Boolean = false,
)

data class Breadcrumb(val name: String, val id: String?)
