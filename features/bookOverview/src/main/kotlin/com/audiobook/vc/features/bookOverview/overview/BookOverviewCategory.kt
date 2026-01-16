package com.audiobook.vc.features.bookOverview.overview

import androidx.annotation.StringRes
import com.audiobook.vc.core.data.Book
import com.audiobook.vc.core.data.BookComparator
import java.util.concurrent.TimeUnit.SECONDS
import com.audiobook.vc.core.strings.R as StringsR

enum class BookOverviewCategory(
  @StringRes val nameRes: Int,
  val comparator: Comparator<Book>,
) {
  CURRENT(
    nameRes = StringsR.string.book_header_current,
    comparator = BookComparator.ByLastPlayed,
  ),
  NOT_STARTED(
    nameRes = StringsR.string.book_header_not_started,
    comparator = BookComparator.ByName,
  ),
  FINISHED(
    nameRes = StringsR.string.book_header_completed,
    comparator = BookComparator.ByLastPlayed,
  ),
  SERIES(
    nameRes = StringsR.string.book_header_series,
    comparator = BookComparator.BySeries,
  ),
}

val Book.category: BookOverviewCategory
  get() {
    return if (position == 0L) {
      BookOverviewCategory.NOT_STARTED
    } else {
      if (position >= duration - SECONDS.toMillis(5)) {
        BookOverviewCategory.FINISHED
      } else {
        BookOverviewCategory.CURRENT
      }
    }
  }
