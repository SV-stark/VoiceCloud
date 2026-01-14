package com.audiobook.vc.core.googledrive

import android.net.Uri
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.runBlocking
import com.audiobook.vc.core.documentfile.CachedDocumentFile

/**
 * Implementation of CachedDocumentFile for Google Drive files.
 * Allows Drive files to be used interchangeably with local DocumentFiles.
 */
class GoogleDriveDocumentFile(
  private val driveFile: DriveFile,
  private val client: GoogleDriveClient,
) : CachedDocumentFile {

  override val children: List<CachedDocumentFile>
    get() = if (driveFile.isFolder) {
      runBlocking {
        client.listFiles(driveFile.id).map { child ->
          GoogleDriveDocumentFile(child, client)
        }
      }
    } else {
      emptyList()
    }

  override val name: String? = driveFile.name

  override val isDirectory: Boolean = driveFile.isFolder

  override val isFile: Boolean = !driveFile.isFolder

  override val length: Long = driveFile.size ?: 0L

  override val lastModified: Long = driveFile.modifiedTime ?: 0L

  /**
   * Returns a special URI scheme for Google Drive files.
   * Format: googledrive://{fileId}
   * 
   * This URI is recognized by the GoogleDriveDataSource for streaming.
   */
  override val uri: Uri
    get() = Uri.Builder()
      .scheme(GOOGLE_DRIVE_SCHEME)
      .authority(driveFile.id)
      .build()

  companion object {
    const val GOOGLE_DRIVE_SCHEME = "googledrive"

    fun isGoogleDriveUri(uri: Uri): Boolean {
      return uri.scheme == GOOGLE_DRIVE_SCHEME
    }

    fun getFileId(uri: Uri): String? {
      return if (isGoogleDriveUri(uri)) uri.authority else null
    }
  }
}

/**
 * Factory for creating GoogleDriveDocumentFile instances.
 */
@Inject
class GoogleDriveDocumentFileFactory(
  private val client: GoogleDriveClient,
) {
  suspend fun create(driveFile: DriveFile): GoogleDriveDocumentFile {
    return GoogleDriveDocumentFile(driveFile, client)
  }

  suspend fun createFromId(fileId: String): GoogleDriveDocumentFile? {
    val driveFile = client.getFile(fileId) ?: return null
    return GoogleDriveDocumentFile(driveFile, client)
  }
}
