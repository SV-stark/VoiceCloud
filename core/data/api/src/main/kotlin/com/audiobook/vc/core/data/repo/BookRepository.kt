package com.audiobook.vc.core.data.repo

import kotlinx.coroutines.flow.Flow
import com.audiobook.vc.core.data.Book
import com.audiobook.vc.core.data.BookContent
import com.audiobook.vc.core.data.BookId

public interface BookRepository {

  public fun flow(): Flow<List<Book>>

  public suspend fun all(): List<Book>

  public fun flow(id: BookId): Flow<Book?>

  public suspend fun get(id: BookId): Book?

  public suspend fun updateBook(
    id: BookId,
    update: (BookContent) -> BookContent,
  )
}
