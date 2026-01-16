package com.audiobook.vc.features.googledrive

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation3.runtime.NavEntry
import com.audiobook.vc.core.common.rootGraphAs
import com.audiobook.vc.core.googledrive.DriveFile
import com.audiobook.vc.core.ui.rememberScoped
import com.audiobook.vc.navigation.Destination
import com.audiobook.vc.navigation.NavEntryProvider
import com.audiobook.vc.navigation.Origin
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides

@ContributesTo(AppScope::class)
interface GoogleDriveFeatureGraph {
  val googleDriveViewModelFactory: GoogleDriveViewModel.Factory
}

@ContributesTo(AppScope::class)
interface GoogleDriveProvider {
  @Provides
  @IntoSet
  fun navEntryProvider(): NavEntryProvider<*> = NavEntryProvider<Destination.GoogleDrive> { key ->
    NavEntry(key) {
      GoogleDriveScreen(key.origin)
    }
  }
}

@Composable
fun GoogleDriveScreen(origin: Origin) {
  val viewModel = rememberScoped {
    rootGraphAs<GoogleDriveFeatureGraph>().googleDriveViewModelFactory.create(origin)
  }
  
  val launcher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
  ) { result ->
    viewModel.onSignInResult(result.data)
  }

  LaunchedEffect(viewModel.state.signInRequired) {
    if (viewModel.state.signInRequired) {
      launcher.launch(viewModel.getSignInIntent())
    }
  }
  
  GoogleDriveBrowser(
    state = viewModel.state,
    onBack = viewModel::onBackClick,
    onFileClick = { file -> viewModel.onFileClick(file) },
    onSelectCurrent = viewModel::onSelectCurrentFolder,
    onRefresh = viewModel::refresh,
    onRetry = viewModel::retry
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleDriveBrowser(
  state: GoogleDriveViewState,
  onBack: () -> Unit,
  onFileClick: (DriveFile) -> Unit,
  onSelectCurrent: () -> Unit,
  onRefresh: () -> Unit,
  onRetry: () -> Unit
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(state.currentFolderName) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          if (!state.isLoading) {
            IconButton(onClick = onRefresh) {
              Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
          }
        }
      )
    },
    bottomBar = {
      if (!state.signInFailed && state.error == null) {
        Box(Modifier.fillMaxWidth().padding(16.dp)) {
          Button(
            onClick = onSelectCurrent,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Select this folder")
          }
        }
      }
    }
  ) { padding ->
    Box(Modifier.padding(padding).fillMaxSize()) {
      when {
        state.signInFailed -> {
          ErrorState(
            title = "Sign-in Failed",
            message = "Could not sign in to Google Drive. Please try again.",
            onRetry = onRetry
          )
        }
        state.error != null -> {
          ErrorState(
            title = "Error",
            message = state.error,
            onRetry = onRetry
          )
        }
        state.isLoading -> {
          CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
        state.files.isEmpty() -> {
          EmptyState()
        }
        else -> {
          PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh
          ) {
            LazyColumn(Modifier.fillMaxSize()) {
              items(state.files) { file ->
                DriveFileItem(file = file, onClick = { onFileClick(file) })
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun EmptyState() {
  Column(
    modifier = Modifier.fillMaxSize().padding(32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Icon(
      imageVector = Icons.Default.Folder,
      contentDescription = null,
      modifier = Modifier.size(64.dp),
      tint = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(16.dp))
    Text(
      text = "This folder is empty",
      style = MaterialTheme.typography.titleMedium
    )
    Text(
      text = "No files or folders found",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}

@Composable
private fun ErrorState(
  title: String,
  message: String,
  onRetry: () -> Unit
) {
  Column(
    modifier = Modifier.fillMaxSize().padding(32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Icon(
      imageVector = Icons.Default.Warning,
      contentDescription = null,
      modifier = Modifier.size(64.dp),
      tint = MaterialTheme.colorScheme.error
    )
    Spacer(Modifier.height(16.dp))
    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium
    )
    Spacer(Modifier.height(8.dp))
    Text(
      text = message,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(24.dp))
    OutlinedButton(onClick = onRetry) {
      Text("Retry")
    }
  }
}

@Composable
fun DriveFileItem(file: DriveFile, onClick: () -> Unit) {
  ListItem(
    headlineContent = { Text(file.name) },
    supportingContent = {
      if (!file.isFolder && file.size != null) {
        Text(formatFileSize(file.size))
      }
    },
    leadingContent = {
      Icon(
        imageVector = getFileIcon(file),
        contentDescription = null,
        tint = if (file.isFolder) {
          MaterialTheme.colorScheme.primary
        } else {
          MaterialTheme.colorScheme.onSurfaceVariant
        }
      )
    },
    modifier = Modifier.clickable(onClick = onClick)
  )
}

/**
 * Get appropriate icon based on MIME type (like AnExplorer's IconUtils).
 */
private fun getFileIcon(file: DriveFile): ImageVector {
  if (file.isFolder) return Icons.Default.Folder
  
  return when {
    file.mimeType.startsWith("audio/") -> Icons.Default.AudioFile
    file.mimeType.startsWith("video/") -> Icons.Default.Movie
    file.mimeType.startsWith("image/") -> Icons.Default.Image
    file.mimeType == "application/pdf" -> Icons.Default.PictureAsPdf
    else -> Icons.AutoMirrored.Filled.InsertDriveFile
  }
}

/**
 * Format file size in human-readable format.
 */
private fun formatFileSize(bytes: Long): String {
  return when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
    else -> "${bytes / (1024 * 1024 * 1024)} GB"
  }
}

