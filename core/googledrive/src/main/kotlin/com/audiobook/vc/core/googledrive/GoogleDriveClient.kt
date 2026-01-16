@file:Suppress("DEPRECATION")
package com.audiobook.vc.core.googledrive

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import com.audiobook.vc.core.logging.api.Logger
import java.io.IOException

/**
 * Represents a file or folder from Google Drive with enhanced metadata.
 */
data class DriveFile(
  val id: String,
  val name: String,
  val mimeType: String,
  val size: Long?,
  val isFolder: Boolean,
  val modifiedTime: Long?,
  /** Thumbnail image URL (available for images, videos, documents) */
  val thumbnailUri: String? = null,
  /** Link to view/open in Google Drive web */
  val webViewLink: String? = null,
  /** Parent folder IDs */
  val parents: List<String>? = null,
  /** File description */
  val description: String? = null,
)

interface GoogleDriveClient {
  suspend fun listFiles(folderId: String? = null): List<DriveFile>
  suspend fun listAudioFolders(): List<DriveFile>
  suspend fun getFile(fileId: String): DriveFile?
  suspend fun getStreamUrl(fileId: String): String?
  /** Get thumbnail URL for a file (if available) */
  suspend fun getThumbnailUrl(fileId: String): String?
  fun isConnected(): Boolean
  fun getSignInIntent(): Intent
  /** List files with pagination support */
  suspend fun listFilesWithPagination(
    folderId: String? = null,
    pageToken: String? = null,
    pageSize: Int = 100
  ): PagedResult<DriveFile>
}

/**
 * Result with pagination support.
 */
data class PagedResult<T>(
  val items: List<T>,
  val nextPageToken: String? = null,
  val hasMore: Boolean = nextPageToken != null
)

@Inject
@ContributesBinding(AppScope::class)
class GoogleDriveClientImpl(
  private val context: Context,
) : GoogleDriveClient {

  companion object {
    private const val MAX_RETRIES = 3
    private const val INITIAL_BACKOFF_MS = 1000L
    private const val FOLDER_MIME_TYPE = "application/vnd.google-apps.folder"
  }

  private fun getDriveService(): Drive? {
    val account = GoogleSignIn.getLastSignedInAccount(context)
    if (account == null) {
      Logger.w("getDriveService: not signed in")
      return null
    }
    Logger.d("getDriveService: account=${account.email}")
    
    val credential = GoogleAccountCredential.usingOAuth2(
      context,
      listOf(DriveScopes.DRIVE_READONLY)
    )
    credential.selectedAccount = account.account

    return Drive.Builder(
      NetHttpTransport(),
      GsonFactory.getDefaultInstance(),
      credential
    )
      .setApplicationName("VoiceCloud")
      .build()
  }

  override fun isConnected(): Boolean {
    return GoogleSignIn.getLastSignedInAccount(context) != null
  }

  /**
   * Retry wrapper with exponential backoff for transient failures.
   */
  private suspend fun <T> withRetry(
    operation: String,
    block: suspend () -> T
  ): T {
    var lastException: Exception? = null
    var backoff = INITIAL_BACKOFF_MS
    
    repeat(MAX_RETRIES) { attempt ->
      try {
        return block()
      } catch (e: IOException) {
        lastException = e
        Logger.w("$operation failed (attempt ${attempt + 1}/$MAX_RETRIES): ${e.message}")
        if (attempt < MAX_RETRIES - 1) {
          delay(backoff)
          backoff *= 2
        }
      } catch (e: Exception) {
        // Non-retryable exception
        throw e
      }
    }
    
    throw lastException ?: IOException("$operation failed after $MAX_RETRIES retries")
  }

  override suspend fun listFiles(folderId: String?): List<DriveFile> {
    val result = listFilesWithPagination(folderId)
    return result.items
  }

  override suspend fun listFilesWithPagination(
    folderId: String?,
    pageToken: String?,
    pageSize: Int
  ): PagedResult<DriveFile> = withContext(Dispatchers.IO) {
    Logger.d("listFiles: folderId=$folderId, pageToken=$pageToken")
    val service = getDriveService()
    if (service == null) {
      Logger.e("listFiles: service is null")
      return@withContext PagedResult(emptyList())
    }
    
    try {
      withRetry("listFiles") {
        val query = buildString {
          if (folderId != null) {
            append("'$folderId' in parents")
          } else {
            append("'root' in parents")
          }
          append(" and trashed = false")
        }

        val request = service.files().list()
          .setQ(query)
          .setSpaces("drive")
          .setFields("nextPageToken, files(id, name, mimeType, size, modifiedTime, thumbnailLink, webViewLink, parents, description)")
          .setOrderBy("folder, name")
          .setPageSize(pageSize)
        
        if (pageToken != null) {
          request.setPageToken(pageToken)
        }
        
        val result: FileList = request.execute()

        val files = result.files?.map { it.toDriveFile() } ?: emptyList()
        Logger.d("listFiles: found ${files.size} files, nextPageToken=${result.nextPageToken}")
        
        PagedResult(
          items = files,
          nextPageToken = result.nextPageToken
        )
      }
    } catch (e: Throwable) {
      Logger.e(e, "listFiles failed")
      PagedResult(emptyList())
    }
  }

  override suspend fun listAudioFolders(): List<DriveFile> = withContext(Dispatchers.IO) {
    val service = getDriveService() ?: return@withContext emptyList()
    
    try {
      withRetry("listAudioFolders") {
        val query = "mimeType = '$FOLDER_MIME_TYPE' and trashed = false"

        val result: FileList = service.files().list()
          .setQ(query)
          .setSpaces("drive")
          .setFields("files(id, name, mimeType, modifiedTime, thumbnailLink, webViewLink, parents)")
          .setOrderBy("name")
          .setPageSize(100)
          .execute()

        result.files?.map { it.toDriveFile() } ?: emptyList()
      }
    } catch (e: Exception) {
      Logger.e(e, "Failed to list folders from Google Drive")
      emptyList()
    }
  }

  override suspend fun getFile(fileId: String): DriveFile? = withContext(Dispatchers.IO) {
    val service = getDriveService() ?: return@withContext null
    
    try {
      withRetry("getFile") {
        val file = service.files().get(fileId)
          .setFields("id, name, mimeType, size, modifiedTime, thumbnailLink, webViewLink, parents, description")
          .execute()
        file.toDriveFile()
      }
    } catch (e: Exception) {
      Logger.e(e, "Failed to get file from Google Drive: $fileId")
      null
    }
  }

  override suspend fun getStreamUrl(fileId: String): String? {
    // For streaming, we use the direct download URL format
    // The actual authentication is handled by the DataSource with OAuth headers
    return "https://www.googleapis.com/drive/v3/files/$fileId?alt=media"
  }

  override suspend fun getThumbnailUrl(fileId: String): String? = withContext(Dispatchers.IO) {
    val service = getDriveService() ?: return@withContext null
    
    try {
      val file = service.files().get(fileId)
        .setFields("thumbnailLink")
        .execute()
      file.thumbnailLink
    } catch (e: Exception) {
      Logger.w("Failed to get thumbnail for $fileId: ${e.message}")
      null
    }
  }

  private fun File.toDriveFile(): DriveFile {
    return DriveFile(
      id = id,
      name = name ?: "Unknown",
      mimeType = mimeType ?: "",
      size = getSize(),
      isFolder = mimeType == FOLDER_MIME_TYPE,
      modifiedTime = modifiedTime?.value,
      thumbnailUri = thumbnailLink,
      webViewLink = webViewLink,
      parents = parents,
      description = description,
    )
  }

  override fun getSignInIntent(): Intent {
    val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
      com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
    )
      .requestEmail()
      .requestScopes(com.google.android.gms.common.api.Scope(DriveScopes.DRIVE_READONLY))
      .build()
    return GoogleSignIn.getClient(context, gso).signInIntent
  }
}

