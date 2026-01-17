package com.audiobook.vc.core.documentfile

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract
import androidx.core.database.getStringOrNull
import com.audiobook.vc.core.logging.api.Logger

internal fun parseContents(
  uri: Uri,
  context: Context,
): List<CachedDocumentFile> {
  Logger.d("ParseContents: Parsing $uri")
  return context.query(uri)?.use { cursor ->
    cursor.parseRows(uri, context)
  } ?: run {
    Logger.w("ParseContents: Query returned null for $uri")
    emptyList()
  }
}

private fun Cursor.parseRows(
  uri: Uri,
  context: Context,
): List<CachedDocumentFile> {
  val files = mutableListOf<CachedDocumentFile>()
  while (moveToNext()) {
    val documentId = getStringOrNull(getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID))
    val mimeType = getStringOrNull(getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE))
    Logger.d("ParseContents: Found documentId=$documentId, mimeType=$mimeType")

    val treeUri = DocumentsContract.buildTreeDocumentUri(
      uri.authority,
      DocumentsContract.getTreeDocumentId(uri),
    )
    val documentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
    val contents = FileContents.readFrom(this)
    if (contents.name?.startsWith(".") == true) {
      Logger.v("Ignoring hidden file $contents")
      continue
    }
    files += RealCachedDocumentFile(context, documentUri, contents)
  }
  Logger.d("ParseContents: Parsed ${files.size} files")
  return files
}

private fun Context.query(uri: Uri): Cursor? {
  return try {
    val treeUri = DocumentsContract.buildTreeDocumentUri(
      uri.authority,
      DocumentsContract.getTreeDocumentId(uri),
    )
    val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
      treeUri,
      DocumentsContract.getDocumentId(uri),
    )
    Logger.d("ParseContents: Querying childrenUri=$childrenUri")
    contentResolver.query(
      childrenUri,
      FileContents.columns,
      null,
      null,
      null,
    )
  } catch (e: Exception) {
    Logger.w(e, "Can't parse contents for $uri")
    null
  }
}
