package com.audiobook.vc.features.googledrive

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
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
    android.util.Log.d("GoogleDriveScreen", "ActivityResult received: resultCode=${result.resultCode}, data=${result.data}")
    viewModel.onSignInResult(result.data)
  }

  LaunchedEffect(viewModel.state.signInRequired) {
    android.util.Log.d("GoogleDriveScreen", "LaunchedEffect: signInRequired=${viewModel.state.signInRequired}")
    if (viewModel.state.signInRequired) {
      android.util.Log.d("GoogleDriveScreen", "Launching sign-in intent")
      launcher.launch(viewModel.getSignInIntent())
    }
  }
  
  GoogleDriveBrowser(
    state = viewModel.state,
    onBack = viewModel::onBackClick,
    onFileClick = { file -> viewModel.onFileClick(file) },
    onSelectCurrent = viewModel::onSelectCurrentFolder
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleDriveBrowser(
  state: GoogleDriveViewState,
  onBack: () -> Unit,
  onFileClick: (DriveFile) -> Unit,
  onSelectCurrent: () -> Unit
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(state.currentFolderName) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        }
      )
    },
    bottomBar = {
      Box(Modifier.fillMaxWidth().padding(16.dp)) {
        Button(
           onClick = onSelectCurrent,
           modifier = Modifier.fillMaxWidth()
        ) {
          Text("Select this folder")
        }
      }
    }
  ) { padding ->
    Box(Modifier.padding(padding).fillMaxSize()) {
      if (state.isLoading) {
        CircularProgressIndicator(Modifier.align(Alignment.Center))
      } else {
        Column {
           // Breadcrumbs could go here
           if (state.files.isEmpty()) {
             Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
               Text("No files found")
             }
           } else {
             LazyColumn {
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
fun DriveFileItem(file: DriveFile, onClick: () -> Unit) {
  ListItem(
    headlineContent = { Text(file.name) },
    leadingContent = {
      Icon(
        imageVector = if (file.isFolder) Icons.Default.Folder else Icons.AutoMirrored.Filled.InsertDriveFile,
        contentDescription = null
      )
    },
    modifier = Modifier.clickable(onClick = onClick)
  )
}
