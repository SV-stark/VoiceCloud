package com.audiobook.vc.features.bookOverview.fileCover

import android.net.Uri
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import com.audiobook.vc.core.data.BookId
import com.audiobook.vc.features.bookOverview.bottomSheet.BottomSheetItem
import com.audiobook.vc.features.bookOverview.bottomSheet.BottomSheetItemViewModel
import com.audiobook.vc.features.bookOverview.di.BookOverviewScope
import com.audiobook.vc.navigation.Destination
import com.audiobook.vc.navigation.Navigator

@BookOverviewScope
@ContributesIntoSet(BookOverviewScope::class)
@Inject
class FileCoverViewModel(private val navigator: Navigator) : BottomSheetItemViewModel {

  private var bookId: BookId? = null

  override suspend fun items(bookId: BookId): List<BottomSheetItem> {
    return listOf(BottomSheetItem.FileCover)
  }

  override suspend fun onItemClick(
    bookId: BookId,
    item: BottomSheetItem,
  ) {
    if (item == BottomSheetItem.FileCover) {
      this.bookId = bookId
    }
  }

  fun onImagePicked(uri: Uri) {
    val bookId = bookId ?: return
    navigator.goTo(Destination.EditCover(bookId, uri))
  }
}
