package com.audiobook.vc.features.bookOverview.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.audiobook.vc.core.data.BookId
import com.audiobook.vc.core.strings.R
import com.audiobook.vc.features.bookOverview.overview.BookOverviewItemViewState

@Composable
fun RecentlyPlayedRow(
  books: List<BookOverviewItemViewState>,
  onBookClick: (BookId) -> Unit,
  onBookLongClick: (BookId) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier) {
    Text(
      text = stringResource(id = R.string.media_session_recent),
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.primary,
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
    LazyRow(
      contentPadding = PaddingValues(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      items(books, key = { it.id.value }) { book ->
        Box(modifier = Modifier.width(160.dp)) {
          GridBook(
            book = book,
            onBookClick = onBookClick,
            onBookLongClick = onBookLongClick,
          )
        }
      }
    }
  }
}
