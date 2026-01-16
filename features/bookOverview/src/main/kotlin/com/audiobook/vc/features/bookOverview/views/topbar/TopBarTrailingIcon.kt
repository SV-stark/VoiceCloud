package com.audiobook.vc.features.bookOverview.views.topbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.audiobook.vc.core.data.BookComparator
import com.audiobook.vc.features.bookOverview.views.BookFolderIcon
import com.audiobook.vc.features.bookOverview.views.SettingsIcon

@Composable
internal fun ColumnScope.TopBarTrailingIcon(
  searchActive: Boolean,
  showAddBookHint: Boolean,
  showFolderPickerIcon: Boolean,
  onBookFolderClick: () -> Unit,
  onSettingsClick: () -> Unit,
  currentSortMode: BookComparator,
  onSortModeChange: (BookComparator) -> Unit,
) {
  AnimatedVisibility(
    visible = !searchActive,
    enter = fadeIn(),
    exit = fadeOut(),
  ) {
    Row {
      SortButton(
        currentSortMode = currentSortMode,
        onSortModeChange = onSortModeChange,
      )
      if (showFolderPickerIcon) {
        BookFolderIcon(withHint = showAddBookHint, onClick = onBookFolderClick)
      }
      SettingsIcon(onSettingsClick)
    }
  }
}

@Composable
private fun SortButton(
  currentSortMode: BookComparator,
  onSortModeChange: (BookComparator) -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }
  Box {
    IconButton(onClick = { expanded = true }) {
      Icon(
        imageVector = Icons.Outlined.Sort,
        contentDescription = "Sort",
      )
    }
    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
    ) {
      val sortOptions = listOf(
        BookComparator.ByLastPlayed to "Last Played",
        BookComparator.ByName to "Name",
        BookComparator.ByAuthor to "Author",
        BookComparator.BySeries to "Series",
        BookComparator.ByDuration to "Duration",
        BookComparator.ByProgress to "Progress",
        BookComparator.ByDateAdded to "Date Added",
      )
      sortOptions.forEach { (comparator, label) ->
        DropdownMenuItem(
          text = { Text(label) },
          onClick = {
            onSortModeChange(comparator)
            expanded = false
          },
          leadingIcon = {
            RadioButton(
              selected = currentSortMode == comparator,
              onClick = null,
            )
          },
        )
      }
    }
  }
}
