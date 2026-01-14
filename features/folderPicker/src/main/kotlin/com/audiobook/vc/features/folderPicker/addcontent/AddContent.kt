package com.audiobook.vc.features.folderPicker.addcontent

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavEntry
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import com.audiobook.vc.core.common.rootGraphAs
import com.audiobook.vc.core.ui.VoiceTheme
import com.audiobook.vc.core.ui.rememberScoped
import com.audiobook.vc.navigation.Destination
import com.audiobook.vc.navigation.NavEntryProvider
import com.audiobook.vc.navigation.Origin

@ContributesTo(AppScope::class)
interface AddContentGraph {
  val viewModelFactory: AddContentViewModel.Factory
}

@ContributesTo(AppScope::class)
interface AddContentProvider {

  @Provides
  @IntoSet
  fun navEntryProvider(): NavEntryProvider<*> = NavEntryProvider<Destination.AddContent> { key ->
    NavEntry(key) {
      AddContent(origin = key.origin)
    }
  }
}

@Composable
fun AddContent(origin: Origin) {
  val viewModel = rememberScoped(origin.name) {
    rootGraphAs<AddContentGraph>().viewModelFactory.create(origin)
  }
  SelectFolder(
    onBack = {
      viewModel.back()
    },
    origin = origin,
    onAdd = { folderType, uri ->
      viewModel.add(uri, folderType)
    },
  )
}

@Composable
@Preview
private fun AddContentPreview() {
  VoiceTheme {
    AddContent(Origin.Default)
  }
}
