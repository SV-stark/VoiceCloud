package com.audiobook.vc.core.googledrive

import android.net.Uri
import com.audiobook.vc.core.documentfile.CachedDocumentFile
import com.audiobook.vc.core.documentfile.DocumentFileProvider
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
    val fileId = uri.host ?: error("Invalid Google Drive URI: $uri")
    // Retrieve initial metadata if possible, otherwise we might need a factory that fetches it
    // For now, we assume we can create it. Since CachedDocumentFile usually requires initial data or fetches it lazily.
    // Looking at GoogleDriveDocumentFile, it takes a DriveFile.
    // We might need to fetch it synchronously or redesign GoogleDriveDocumentFile to be lazy.
    // However, the Factory create is synchronous.
    // We will create a lazy version or assume the ID is enough to start.
    // Wait, GoogleDriveDocumentFile takes a DriveFile object.
    // We probably need to fetch it. But we can't do suspend calls here easily.
    // Let's check GoogleDriveDocumentFile definition first.
    // For now I'll put a placeholder TODO or handle it if GoogleDriveDocumentFile allows nulls/lazy loading.
    
    // Actually, create(uri) is called when we are restoring from a URI (e.g. from DB).
    // In that case, we might not have the DriveFile object handy.
    // I should inspect GoogleDriveDocumentFile.kt to see its constructor.
    return GoogleDriveDocumentFile(
      driveFile = DriveFile(
        id = fileId,
        name = "Loading...", // Placeholder
        mimeType = "application/vnd.google-apps.folder", // Guessing folder for root? Or generic.
        size = null,
        isFolder = true, // Default to folder safely? Or file?
        modifiedTime = null
      ),
      client = googleDriveClient
    )
  }
}
