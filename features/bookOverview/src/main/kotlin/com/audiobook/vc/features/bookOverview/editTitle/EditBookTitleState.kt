package com.audiobook.vc.features.bookOverview.editTitle

import com.audiobook.vc.core.data.BookId

internal data class EditBookTitleState(
  val title: String,
  val bookId: BookId,
) {

  val confirmButtonEnabled: Boolean = title.trim().isNotEmpty()
}
