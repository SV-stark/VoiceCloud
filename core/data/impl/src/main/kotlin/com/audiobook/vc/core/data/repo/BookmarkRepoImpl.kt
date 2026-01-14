package com.audiobook.vc.core.data.repo

import androidx.room.RoomDatabase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.audiobook.vc.core.data.Book
import com.audiobook.vc.core.data.BookContent
import com.audiobook.vc.core.data.Bookmark
import com.audiobook.vc.core.data.repo.internals.dao.BookmarkDao
import com.audiobook.vc.core.data.repo.internals.transaction
import com.audiobook.vc.core.data.runForMaxSqlVariableNumber
import com.audiobook.vc.core.logging.api.Logger
import java.time.Instant

@Inject
@ContributesBinding(AppScope::class)
public class BookmarkRepoImpl
internal constructor(
  private val dao: BookmarkDao,
  private val appDb: RoomDatabase,
) : BookmarkRepo {

  override suspend fun deleteBookmark(id: Bookmark.Id) {
    dao.deleteBookmark(id)
  }

  override suspend fun addBookmark(bookmark: Bookmark) {
    dao.addBookmark(bookmark)
  }

  override suspend fun addBookmarkAtBookPosition(
    book: Book,
    title: String?,
    setBySleepTimer: Boolean,
  ): Bookmark {
    return withContext(Dispatchers.IO) {
      val bookMark = Bookmark(
        title = title,
        time = book.content.positionInChapter,
        id = Bookmark.Id.random(),
        addedAt = Instant.now(),
        setBySleepTimer = setBySleepTimer,
        chapterId = book.content.currentChapter,
        bookId = book.id,
      )
      addBookmark(bookMark)
      Logger.v("Added bookmark=$bookMark")
      bookMark
    }
  }

  override suspend fun bookmarks(book: BookContent): List<Bookmark> {
    val chapters = book.chapters
    return appDb.transaction {
      chapters.runForMaxSqlVariableNumber {
        dao.allForChapters(it)
      }
    }
  }
}
