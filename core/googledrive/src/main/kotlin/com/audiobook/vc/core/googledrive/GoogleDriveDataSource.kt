@file:Suppress("DEPRECATION")
package com.audiobook.vc.core.googledrive

import android.content.Context
import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.BaseDataSource
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.HttpDataSource
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.runBlocking
import com.audiobook.vc.core.logging.api.Logger
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

/**
 * ExoPlayer DataSource for streaming audio from Google Drive.
 * 
 * This DataSource:
 * - Authenticates requests with OAuth access tokens via GoogleDriveAuthManager
 * - Supports HTTP Range requests for seeking
 * - Streams content without full download
 * - Auto-refreshes tokens on HTTP 401 responses
 * - Retries on transient network errors
 */
@UnstableApi
class GoogleDriveDataSource(
  private val context: Context,
  private val authManager: GoogleDriveAuthManager,
) : BaseDataSource(/* isNetwork = */ true) {

  companion object {
    private const val MAX_RETRIES = 3
    private const val CONNECT_TIMEOUT_MS = 15_000
    private const val READ_TIMEOUT_MS = 30_000
    
    // Transient error codes that warrant retry
    private val RETRYABLE_STATUS_CODES = setOf(408, 429, 500, 502, 503, 504)
  }

  private var connection: HttpURLConnection? = null
  private var inputStream: InputStream? = null
  private var bytesRemaining: Long = 0
  private var opened = false
  private var currentDataSpec: DataSpec? = null

  override fun open(dataSpec: DataSpec): Long {
    currentDataSpec = dataSpec
    val uri = dataSpec.uri
    
    // Check if this is a Google Drive URI
    val fileId = GoogleDriveDocumentFile.getFileId(uri)
    if (fileId == null) {
      throw IOException("Not a Google Drive URI: $uri")
    }

    val streamUrl = "https://www.googleapis.com/drive/v3/files/$fileId?alt=media"
    
    return openWithRetry(streamUrl, dataSpec, retriesRemaining = MAX_RETRIES, forceRefreshToken = false)
  }

  private fun openWithRetry(
    streamUrl: String,
    dataSpec: DataSpec,
    retriesRemaining: Int,
    forceRefreshToken: Boolean
  ): Long {
    // Get access token (force refresh if requested, e.g., after 401)
    val accessToken = runBlocking {
      if (forceRefreshToken) {
        Logger.d("Forcing token refresh before retry")
        authManager.refreshToken()
      } else {
        authManager.getAccessTokenCached()
      }
    } ?: throw IOException("Not authenticated with Google Drive")

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
        
        connectTimeout = CONNECT_TIMEOUT_MS
        readTimeout = READ_TIMEOUT_MS
        connect()
      }

      val responseCode = connection!!.responseCode
      
      // Handle 401 Unauthorized - token may have expired
      if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
        closeConnection()
        if (retriesRemaining > 0) {
          Logger.w("HTTP 401 received, refreshing token and retrying (retries left: ${retriesRemaining - 1})")
          return openWithRetry(streamUrl, dataSpec, retriesRemaining - 1, forceRefreshToken = true)
        }
        throw IOException("Authentication failed after token refresh")
      }
      
      // Handle transient errors (503, 429, etc.)
      if (responseCode in RETRYABLE_STATUS_CODES) {
        closeConnection()
        if (retriesRemaining > 0) {
          Logger.w("HTTP $responseCode received, retrying (retries left: ${retriesRemaining - 1})")
          Thread.sleep(1000L * (MAX_RETRIES - retriesRemaining + 1)) // Simple backoff
          return openWithRetry(streamUrl, dataSpec, retriesRemaining - 1, forceRefreshToken = false)
        }
      }
      
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
      
      Logger.d("Opened Google Drive stream, bytes remaining: $bytesRemaining")
      return bytesRemaining
    } catch (e: SocketTimeoutException) {
      closeConnection()
      if (retriesRemaining > 0) {
        Logger.w("Timeout opening stream, retrying (retries left: ${retriesRemaining - 1})")
        return openWithRetry(streamUrl, dataSpec, retriesRemaining - 1, forceRefreshToken = false)
      }
      throw IOException("Timeout opening Google Drive stream after retries", e)
    } catch (e: HttpDataSource.InvalidResponseCodeException) {
      throw e
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
    closeConnection()
    currentDataSpec = null
  }

  private fun closeConnection() {
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
}

/**
 * Factory for creating GoogleDriveDataSource instances.
 */
@UnstableApi
@Inject
class GoogleDriveDataSourceFactory(
  private val context: Context,
  private val authManager: GoogleDriveAuthManager,
) : DataSource.Factory {

  override fun createDataSource(): DataSource {
    return GoogleDriveDataSource(context, authManager)
  }
}

