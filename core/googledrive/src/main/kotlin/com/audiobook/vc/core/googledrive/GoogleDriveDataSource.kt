package com.audiobook.vc.core.googledrive

import android.content.Context
import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.BaseDataSource
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.HttpDataSource
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.drive.DriveScopes
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.runBlocking
import com.audiobook.vc.core.logging.api.Logger
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * ExoPlayer DataSource for streaming audio from Google Drive.
 * 
 * This DataSource:
 * - Authenticates requests with OAuth access tokens
 * - Supports HTTP Range requests for seeking
 * - Streams content without full download
 */
@UnstableApi
class GoogleDriveDataSource(
  private val context: Context,
) : BaseDataSource(/* isNetwork = */ true) {

  private var connection: HttpURLConnection? = null
  private var inputStream: InputStream? = null
  private var bytesRemaining: Long = 0
  private var opened = false

  override fun open(dataSpec: DataSpec): Long {
    val uri = dataSpec.uri
    
    // Check if this is a Google Drive URI
    val fileId = GoogleDriveDocumentFile.getFileId(uri)
    if (fileId == null) {
      throw IOException("Not a Google Drive URI: $uri")
    }

    val streamUrl = "https://www.googleapis.com/drive/v3/files/$fileId?alt=media"
    
    // Get access token
    val accessToken = getAccessToken()
      ?: throw IOException("Not authenticated with Google Drive")

    try {
      val url = URL(streamUrl)
      connection = (url.openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        setRequestProperty("Authorization", "Bearer $accessToken")
        
        // Support range requests for seeking
        if (dataSpec.position > 0 || dataSpec.length != C.LENGTH_UNSET.toLong()) {
          val rangeHeader = buildString {
            append("bytes=")
            append(dataSpec.position)
            append("-")
            if (dataSpec.length != C.LENGTH_UNSET.toLong()) {
              append(dataSpec.position + dataSpec.length - 1)
            }
          }
          setRequestProperty("Range", rangeHeader)
        }
        
        connectTimeout = 15_000
        readTimeout = 30_000
        connect()
      }

      val responseCode = connection!!.responseCode
      if (responseCode !in 200..299) {
        throw HttpDataSource.InvalidResponseCodeException(
          responseCode,
          connection!!.responseMessage,
          null,
          emptyMap(),
          dataSpec,
          byteArrayOf()
        )
      }

      inputStream = connection!!.inputStream
      
      // Calculate bytes remaining
      val contentLength = connection!!.getHeaderField("Content-Length")?.toLongOrNull() ?: C.LENGTH_UNSET.toLong()
      bytesRemaining = if (dataSpec.length != C.LENGTH_UNSET.toLong()) {
        dataSpec.length
      } else {
        contentLength
      }

      opened = true
      transferStarted(dataSpec)
      
      return bytesRemaining
    } catch (e: Exception) {
      Logger.e(e, "Failed to open Google Drive stream")
      throw IOException("Failed to open Google Drive stream", e)
    }
  }

  override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
    if (bytesRemaining == 0L) {
      return C.RESULT_END_OF_INPUT
    }

    val stream = inputStream ?: return C.RESULT_END_OF_INPUT
    
    val bytesToRead = if (bytesRemaining != C.LENGTH_UNSET.toLong()) {
      minOf(length.toLong(), bytesRemaining).toInt()
    } else {
      length
    }

    val bytesRead = try {
      stream.read(buffer, offset, bytesToRead)
    } catch (e: IOException) {
      throw IOException("Error reading from Google Drive stream", e)
    }

    if (bytesRead == -1) {
      return C.RESULT_END_OF_INPUT
    }

    if (bytesRemaining != C.LENGTH_UNSET.toLong()) {
      bytesRemaining -= bytesRead
    }
    
    bytesTransferred(bytesRead)
    return bytesRead
  }

  override fun getUri(): Uri? {
    return connection?.url?.toString()?.let { Uri.parse(it) }
  }

  override fun close() {
    if (opened) {
      opened = false
      transferEnded()
    }
    
    try {
      inputStream?.close()
    } catch (e: IOException) {
      Logger.w("Error closing input stream: ${e.message}")
    }
    inputStream = null
    
    connection?.disconnect()
    connection = null
    bytesRemaining = 0
  }

  private fun getAccessToken(): String? {
    val account = GoogleSignIn.getLastSignedInAccount(context) ?: return null
    return try {
      val credential = GoogleAccountCredential.usingOAuth2(
        context, 
        listOf(DriveScopes.DRIVE_READONLY)
      )
      credential.selectedAccount = account.account
      // This blocks but is called on ExoPlayer's IO thread
      runBlocking { credential.token }
    } catch (e: Exception) {
      Logger.e(e, "Failed to get access token for streaming")
      null
    }
  }
}

/**
 * Factory for creating GoogleDriveDataSource instances.
 */
@UnstableApi
@Inject
class GoogleDriveDataSourceFactory(
  private val context: Context,
) : DataSource.Factory {

  override fun createDataSource(): DataSource {
    return GoogleDriveDataSource(context)
  }
}
