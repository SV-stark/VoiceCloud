package com.audiobook.vc.core.googledrive

import android.net.Uri
import com.audiobook.vc.core.documentfile.CachedDocumentFile
import com.audiobook.vc.core.documentfile.DocumentFileProvider
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject

@ContributesIntoSet(AppScope::class)
@Inject
class GoogleDriveDocumentFileProvider(
  private val googleDriveClient: GoogleDriveClient,
) : DocumentFileProvider {
  override fun canHandle(uri: Uri): Boolean {
    return uri.scheme == "googledrive"
  }

  override fun create(uri: Uri): CachedDocumentFile {
    val fileId = uri.host ?: uri.authority ?: error("Invalid Google Drive URI: $uri")
    
    // Fetch real metadata synchronously, since DocumentFileProvider.create is synchronous.
    // This is called on IO dispatcher from SelectFolderTypeViewModel.
    val driveFile = try {
      kotlinx.coroutines.runBlocking {
        googleDriveClient.getFile(fileId)
      }
    } catch (e: Exception) {
      com.audiobook.vc.core.logging.api.Logger.e(e, "Failed to fetch Google Drive file: $fileId")
      null
    }
    
    return GoogleDriveDocumentFile(
      driveFile = driveFile ?: DriveFile(
        id = fileId,
        name = fileId, // Use ID as fallback name
        mimeType = "application/vnd.google-apps.folder",
        size = null,
        isFolder = true,
        modifiedTime = null
      ),
      client = googleDriveClient
    )
  }
}
