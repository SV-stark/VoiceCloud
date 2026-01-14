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
import com.audiobook.vc.core.googledrive.GoogleDriveClient
import com.audiobook.vc.core.googledrive.GoogleDriveDocumentFile
import com.audiobook.vc.features.folderPicker.folderPicker.FileTypeSelection
import com.audiobook.vc.navigation.Navigator
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Inject
class GoogleDriveViewModel @AssistedInject constructor(
  private val googleDriveClient: GoogleDriveClient,
  private val dispatcherProvider: DispatcherProvider,
  private val navigator: Navigator,
  private val audiobookFolders: AudiobookFolders,
  @Assisted private val origin: Origin,
) : ViewModel() {

  var state by mutableStateOf(GoogleDriveViewState())
    private set

  private val breadcrumbs = mutableListOf<Breadcrumb>()

  init {
    loadFiles(null) // Root
  }

  fun loadFiles(folderId: String?, folderName: String? = null) {
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

      // We treat the root of added folder as 'Root' type typically, assuming user added a library.
      // Or we can forward to 'Select Folder Type' screen? 
      // SelectFolderTypeViewModel usually handles the 'Type' selection (Audiobooks vs Single Book).
      // Here we act as a file picker.
      // So we should navigate to SelectFolderType?
      // Yes!
      
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
  val breadcrumbs: List<Breadcrumb> = emptyList()
)

data class Breadcrumb(val name: String, val id: String?)
