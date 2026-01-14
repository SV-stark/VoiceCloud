package com.audiobook.vc.features.bookOverview.internetCover

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
class InternetCoverViewModel(private val navigator: Navigator) : BottomSheetItemViewModel {

  override suspend fun items(bookId: BookId): List<BottomSheetItem> {
    return listOf(BottomSheetItem.InternetCover)
  }

  override suspend fun onItemClick(
    bookId: BookId,
    item: BottomSheetItem,
  ) {
    if (item == BottomSheetItem.InternetCover) {
      navigator.goTo(Destination.CoverFromInternet(bookId))
    }
  }
}
