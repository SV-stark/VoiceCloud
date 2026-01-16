package com.audiobook.vc.features.bookOverview.views.topbar

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.audiobook.vc.core.data.BookComparator
import com.audiobook.vc.core.data.BookId
import com.audiobook.vc.features.bookOverview.search.BookSearchContent
import com.audiobook.vc.features.bookOverview.search.BookSearchViewState

@Composable
internal fun ColumnScope.BookOverviewSearchBar(
  horizontalPadding: Dp,
  onQueryChange: (String) -> Unit,
  onActiveChange: (Boolean) -> Unit,
  onBookFolderClick: () -> Unit,
  onSettingsClick: () -> Unit,
  onSearchBookClick: (BookId) -> Unit,
  searchActive: Boolean,
  showAddBookHint: Boolean,
  showFolderPickerIcon: Boolean,
  searchViewState: BookSearchViewState,
  currentSortMode: BookComparator,
  onSortModeChange: (BookComparator) -> Unit,
) {
  SearchBar(
    inputField = {
      SearchBarDefaults.InputField(
        query = if (searchActive) {
          searchViewState.query
        } else {
          ""
        },
        onQueryChange = onQueryChange,
        onSearch = onQueryChange,
        expanded = searchActive,
        onExpandedChange = onActiveChange,
        leadingIcon = {
          TopBarLeadingIcon(
            searchActive = searchActive,
            onActiveChange = onActiveChange,
          )
        },
        trailingIcon = {
          TopBarTrailingIcon(
            searchActive = searchActive,
            showAddBookHint = showAddBookHint,
            showFolderPickerIcon = showFolderPickerIcon,
            onBookFolderClick = onBookFolderClick,
            onSettingsClick = onSettingsClick,
            currentSortMode = currentSortMode,
            onSortModeChange = onSortModeChange,
          )
        },
      )
    },
    expanded = searchActive,
    onExpandedChange = onActiveChange,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = horizontalPadding),
    content = {
      BookSearchContent(
        viewState = searchViewState,
        contentPadding = PaddingValues(),
        onQueryChange = onQueryChange,
        onBookClick = onSearchBookClick,
      )
    },
  )
}
