package com.audiobook.vc.core.data.repo

import com.audiobook.vc.core.data.Book
import com.audiobook.vc.core.data.BookContent
import com.audiobook.vc.core.data.Bookmark

public interface BookmarkRepo {
  public suspend fun deleteBookmark(id: Bookmark.Id)

  public suspend fun addBookmark(bookmark: Bookmark)

  @IgnorableReturnValue
  public suspend fun addBookmarkAtBookPosition(
    book: Book,
    title: String?,
    setBySleepTimer: Boolean,
  ): Bookmark

  public suspend fun bookmarks(book: BookContent): List<Bookmark>
}
