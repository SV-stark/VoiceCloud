package com.audiobook.vc.core.sleeptimer

import androidx.datastore.core.DataStore
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.first
import com.audiobook.vc.core.data.BookId
import com.audiobook.vc.core.data.repo.BookRepository
import com.audiobook.vc.core.data.repo.BookmarkRepo
import com.audiobook.vc.core.data.store.CurrentBookStore

@Inject
class CreateBookmarkAtCurrentPosition(
  private val bookmarkRepo: BookmarkRepo,
  private val bookRepository: BookRepository,
  @CurrentBookStore
  private val currentBookStore: DataStore<BookId?>,
) {

  suspend fun create() {
    val currentBookId = currentBookStore.data.first() ?: return
    val currentBook = bookRepository.get(currentBookId) ?: return
    bookmarkRepo.addBookmarkAtBookPosition(
      book = currentBook,
      title = null,
      setBySleepTimer = true,
    )
  }
}
