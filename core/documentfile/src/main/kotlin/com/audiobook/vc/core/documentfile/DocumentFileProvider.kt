package com.audiobook.vc.core.documentfile

import android.net.Uri

interface DocumentFileProvider {
  /**
   * Returns true if this provider can handle the given [uri].
   */
  fun canHandle(uri: Uri): Boolean

  /**
   * Creates a [CachedDocumentFile] for the given [uri].
   * Should only be called if [canHandle] returns true.
   */
  fun create(uri: Uri): CachedDocumentFile
}
