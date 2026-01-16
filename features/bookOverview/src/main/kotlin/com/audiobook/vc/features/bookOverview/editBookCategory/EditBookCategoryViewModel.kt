package com.audiobook.vc.features.bookOverview.editBookCategory

import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import com.audiobook.vc.core.data.BookId
import com.audiobook.vc.core.data.repo.BookRepository
import com.audiobook.vc.features.bookOverview.bottomSheet.BottomSheetItem
import com.audiobook.vc.features.bookOverview.bottomSheet.BottomSheetItemViewModel
import com.audiobook.vc.features.bookOverview.di.BookOverviewScope
import com.audiobook.vc.features.bookOverview.overview.BookOverviewCategory
import com.audiobook.vc.features.bookOverview.overview.category
import java.time.Instant

@BookOverviewScope
@ContributesIntoSet(BookOverviewScope::class)
@Inject
class EditBookCategoryViewModel(private val repo: BookRepository) : BottomSheetItemViewModel {

  override suspend fun items(bookId: BookId): List<BottomSheetItem> {
    val book = repo.get(bookId) ?: return emptyList()
    return when (book.category) {
      BookOverviewCategory.CURRENT -> listOf(
        BottomSheetItem.BookCategoryMarkAsNotStarted,
        BottomSheetItem.BookCategoryMarkAsCompleted,
      )
      BookOverviewCategory.NOT_STARTED -> listOf(
        BottomSheetItem.BookCategoryMarkAsCurrent,
        BottomSheetItem.BookCategoryMarkAsCompleted,
      )
      BookOverviewCategory.FINISHED -> listOf(
        BottomSheetItem.BookCategoryMarkAsCurrent,
        BottomSheetItem.BookCategoryMarkAsNotStarted,
      )
      BookOverviewCategory.SERIES -> emptyList()
    }
  }

  override suspend fun onItemClick(
    bookId: BookId,
    item: BottomSheetItem,
  ) {
    val book = repo.get(bookId) ?: return

    val (currentChapter, positionInChapter) = when (item) {
      BottomSheetItem.BookCategoryMarkAsCurrent -> {
        book.chapters.first().id to 1L
      }
      BottomSheetItem.BookCategoryMarkAsNotStarted -> {
        book.chapters.first().id to 0L
      }
      BottomSheetItem.BookCategoryMarkAsCompleted -> {
        val lastChapter = book.chapters.last()
        lastChapter.id to lastChapter.duration
      }
      else -> return
    }

    repo.updateBook(book.id) {
      it.copy(
        currentChapter = currentChapter,
        positionInChapter = positionInChapter,
        lastPlayedAt = Instant.now(),
      )
    }
  }
}
