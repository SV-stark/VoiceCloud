package com.audiobook.vc.features.bookOverview.bottomSheet

import com.audiobook.vc.core.data.BookId

interface BottomSheetItemViewModel {

  suspend fun items(bookId: BookId): List<BottomSheetItem>
  suspend fun onItemClick(
    bookId: BookId,
    item: BottomSheetItem,
  )
}
