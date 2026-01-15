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
import kotlinx.coroutines.withContext
import com.audiobook.vc.core.logging.api.Logger

data class DriveFile(
  val id: String,
  val name: String,
  val mimeType: String,
  val size: Long?,
  val isFolder: Boolean,
  val modifiedTime: Long?,
)

interface GoogleDriveClient {
  suspend fun listFiles(folderId: String? = null): List<DriveFile>
  suspend fun listAudioFolders(): List<DriveFile>
  suspend fun getFile(fileId: String): DriveFile?
  suspend fun getStreamUrl(fileId: String): String?
  fun isConnected(): Boolean
  fun getSignInIntent(): Intent
}

@Inject
@ContributesBinding(AppScope::class)
class GoogleDriveClientImpl(
  private val context: Context,
) : GoogleDriveClient {

  private fun getDriveService(): Drive? {
    val account = GoogleSignIn.getLastSignedInAccount(context) ?: return null
    
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

  override suspend fun listFiles(folderId: String?): List<DriveFile> = withContext(Dispatchers.IO) {
    val service = getDriveService() ?: return@withContext emptyList()
    
    try {
      val query = buildString {
        if (folderId != null) {
          append("'$folderId' in parents")
        } else {
          append("'root' in parents")
        }
        append(" and trashed = false")
      }

      val result: FileList = service.files().list()
        .setQ(query)
        .setSpaces("drive")
        .setFields("files(id, name, mimeType, size, modifiedTime)")
        .setOrderBy("folder, name")
        .setPageSize(100)
        .execute()

      result.files?.map { it.toDriveFile() } ?: emptyList()
    } catch (e: Exception) {
      Logger.e(e, "Failed to list files from Google Drive")
      emptyList()
    }
  }

  override suspend fun listAudioFolders(): List<DriveFile> = withContext(Dispatchers.IO) {
    val service = getDriveService() ?: return@withContext emptyList()
    
    try {
      // List folders that contain audio files
      val query = "mimeType = 'application/vnd.google-apps.folder' and trashed = false"

      val result: FileList = service.files().list()
        .setQ(query)
        .setSpaces("drive")
        .setFields("files(id, name, mimeType, modifiedTime)")
        .setOrderBy("name")
        .setPageSize(100)
        .execute()

      result.files?.map { it.toDriveFile() } ?: emptyList()
    } catch (e: Exception) {
      Logger.e(e, "Failed to list folders from Google Drive")
      emptyList()
    }
  }

  override suspend fun getFile(fileId: String): DriveFile? = withContext(Dispatchers.IO) {
    val service = getDriveService() ?: return@withContext null
    
    try {
      val file = service.files().get(fileId)
        .setFields("id, name, mimeType, size, modifiedTime")
        .execute()
      file.toDriveFile()
    } catch (e: Exception) {
      Logger.e(e, "Failed to get file from Google Drive")
      null
    }
  }

  override suspend fun getStreamUrl(fileId: String): String? {
    // For streaming, we use the direct download URL format
    // The actual authentication is handled by the DataSource with OAuth headers
    return "https://www.googleapis.com/drive/v3/files/$fileId?alt=media"
  }

  private fun File.toDriveFile(): DriveFile {
    return DriveFile(
      id = id,
      name = name ?: "Unknown",
      mimeType = mimeType ?: "",
      size = getSize(),
      isFolder = mimeType == "application/vnd.google-apps.folder",
      modifiedTime = modifiedTime?.value,
    )
  }

  override fun getSignInIntent(): Intent {
    val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
      .requestEmail()
      .requestScopes(com.google.android.gms.common.api.Scope(DriveScopes.DRIVE_READONLY))
      .build()
    return GoogleSignIn.getClient(context, gso).signInIntent
  }
}
