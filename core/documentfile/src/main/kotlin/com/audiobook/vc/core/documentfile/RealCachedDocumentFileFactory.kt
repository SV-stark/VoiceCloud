package com.audiobook.vc.core.documentfile

import android.content.Context
import android.net.Uri
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@ContributesBinding(AppScope::class)
@Inject
class RealCachedDocumentFileFactory(
  private val context: Context,
  private val providers: Set<@JvmSuppressWildcards DocumentFileProvider>,
) : CachedDocumentFileFactory {
  override fun create(uri: Uri): CachedDocumentFile {
    return providers.firstOrNull { it.canHandle(uri) }?.create(uri)
      ?: RealCachedDocumentFile(context = context, uri = uri, preFilledContent = null)
  }
}
