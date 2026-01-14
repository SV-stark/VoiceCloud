package com.audiobook.vc.features.folderPicker.addcontent

import android.net.Uri
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import com.audiobook.vc.core.data.folders.AudiobookFolders
import com.audiobook.vc.core.data.folders.FolderType
import com.audiobook.vc.features.folderPicker.folderPicker.FileTypeSelection
import com.audiobook.vc.navigation.Destination
import com.audiobook.vc.navigation.Destination.OnboardingCompletion
import com.audiobook.vc.navigation.Destination.SelectFolderType
import com.audiobook.vc.navigation.Navigator
import com.audiobook.vc.navigation.Origin

@AssistedInject
class AddContentViewModel(
  private val audiobookFolders: AudiobookFolders,
  private val navigator: Navigator,
  @Assisted
  private val origin: Origin,
) {

  internal fun add(
    uri: Uri,
    type: FileTypeSelection,
  ) {
    when (type) {
      FileTypeSelection.File -> {
        audiobookFolders.add(uri, FolderType.SingleFile)
        when (origin) {
          Origin.Default -> {
            navigator.setRoot(Destination.BookOverview)
          }
          Origin.Onboarding -> {
            navigator.goTo(OnboardingCompletion)
          }
        }
      }
      FileTypeSelection.Folder -> {
        navigator.goTo(
          SelectFolderType(
            uri = uri,
            origin = origin,
          ),
        )
      }
    }
  }

  internal fun back() {
    navigator.goBack()
  }

  @AssistedFactory
  interface Factory {
    fun create(origin: Origin): AddContentViewModel
  }
}
