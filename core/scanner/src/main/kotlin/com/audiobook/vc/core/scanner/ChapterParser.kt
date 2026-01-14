package com.audiobook.vc.core.scanner

import dev.zacsweers.metro.Inject
import com.audiobook.vc.core.data.Chapter
import com.audiobook.vc.core.data.ChapterId
import com.audiobook.vc.core.data.isAudioFile
import com.audiobook.vc.core.data.repo.ChapterRepo
import com.audiobook.vc.core.data.repo.getOrPut
import com.audiobook.vc.core.documentfile.CachedDocumentFile
import java.time.Instant

@Inject
internal class ChapterParser(
  private val chapterRepo: ChapterRepo,
  private val mediaAnalyzer: MediaAnalyzer,
) {

  suspend fun parse(documentFile: CachedDocumentFile): List<Chapter> {
    val result = mutableListOf<Chapter>()

    suspend fun parseChapters(file: CachedDocumentFile) {
      if (file.isAudioFile()) {
        val id = ChapterId(file.uri)
        val chapter = chapterRepo.getOrPut(id, Instant.ofEpochMilli(file.lastModified)) {
          val metaData = mediaAnalyzer.analyze(file) ?: return@getOrPut null
          Chapter(
            id = id,
            duration = metaData.duration,
            fileLastModified = Instant.ofEpochMilli(file.lastModified),
            name = metaData.title ?: metaData.fileName,
            markData = metaData.chapters,
          )
        }
        if (chapter != null) {
          result.add(chapter)
        }
      } else if (file.isDirectory) {
        file.children
          .forEach {
            parseChapters(it)
          }
      }
    }

    parseChapters(file = documentFile)
    return result.sorted()
  }
}
