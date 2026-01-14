package com.audiobook.vc.core.googledrive

import android.net.Uri
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.TransferListener
import dev.zacsweers.metro.Inject

/**
 * A composite DataSource.Factory that delegates to either the local DataSource
 * or Google Drive DataSource based on the URI scheme.
 */
@UnstableApi
@Inject
class CompositeDataSourceFactory(
  private val localDataSourceFactory: DataSource.Factory,
  private val googleDriveDataSourceFactory: GoogleDriveDataSourceFactory,
) : DataSource.Factory {

  override fun createDataSource(): DataSource {
    return CompositeDataSource(
      localDataSourceFactory.createDataSource(),
      googleDriveDataSourceFactory.createDataSource(),
    )
  }
}

/**
 * DataSource that delegates to the appropriate underlying DataSource
 * based on the URI scheme.
 */
@UnstableApi
private class CompositeDataSource(
  private val localDataSource: DataSource,
  private val googleDriveDataSource: DataSource,
) : DataSource {

  private var activeDataSource: DataSource? = null

  override fun open(dataSpec: DataSpec): Long {
    activeDataSource = selectDataSource(dataSpec.uri)
    return activeDataSource!!.open(dataSpec)
  }

  override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
    return activeDataSource?.read(buffer, offset, length)
      ?: throw IllegalStateException("DataSource not opened")
  }

  override fun getUri(): Uri? {
    return activeDataSource?.uri
  }

  override fun close() {
    activeDataSource?.close()
    activeDataSource = null
  }

  override fun addTransferListener(transferListener: TransferListener) {
    localDataSource.addTransferListener(transferListener)
    googleDriveDataSource.addTransferListener(transferListener)
  }

  private fun selectDataSource(uri: Uri): DataSource {
    return if (GoogleDriveDocumentFile.isGoogleDriveUri(uri)) {
      googleDriveDataSource
    } else {
      localDataSource
    }
  }
}
