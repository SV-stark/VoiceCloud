package com.audiobook.vc.core.data.folders

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import com.audiobook.vc.core.documentfile.CachedDocumentFile

public interface AudiobookFolders {
  public fun all(): Flow<Map<FolderType, List<DocumentFileWithUri>>>

  public fun add(
    uri: Uri,
    type: FolderType,
  )

  public fun remove(
    uri: Uri,
    folderType: FolderType,
  )

  public suspend fun hasAnyFolders(): Boolean
}

public data class DocumentFileWithUri(
  val documentFile: CachedDocumentFile,
  val uri: Uri,
)
