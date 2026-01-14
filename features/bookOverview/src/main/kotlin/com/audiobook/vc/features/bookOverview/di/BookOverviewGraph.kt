package com.audiobook.vc.features.bookOverview.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Scope
import com.audiobook.vc.features.bookOverview.bottomSheet.BottomSheetViewModel
import com.audiobook.vc.features.bookOverview.deleteBook.DeleteBookViewModel
import com.audiobook.vc.features.bookOverview.editTitle.EditBookTitleViewModel
import com.audiobook.vc.features.bookOverview.fileCover.FileCoverViewModel
import com.audiobook.vc.features.bookOverview.overview.BookOverviewViewModel

@Scope
annotation class BookOverviewScope

@GraphExtension(scope = BookOverviewScope::class)
@BookOverviewScope
interface BookOverviewGraph {
  val bookOverviewViewModel: BookOverviewViewModel
  val editBookTitleViewModel: EditBookTitleViewModel
  val bottomSheetViewModel: BottomSheetViewModel
  val deleteBookViewModel: DeleteBookViewModel
  val fileCoverViewModel: FileCoverViewModel

  @GraphExtension.Factory
  @ContributesTo(AppScope::class)
  interface Factory {
    fun create(): BookOverviewGraph

    @ContributesTo(AppScope::class)
    interface Provider {
      val bookOverviewGraphProviderFactory: Factory
    }
  }
}
