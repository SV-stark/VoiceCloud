package com.audiobook.vc.features.settings.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavEntry
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import com.audiobook.vc.core.common.rootGraphAs
import com.audiobook.vc.core.ui.VoiceTheme
import com.audiobook.vc.core.ui.rememberScoped
import com.audiobook.vc.features.settings.SettingsListener
import com.audiobook.vc.features.settings.SettingsViewModel
import com.audiobook.vc.features.settings.SettingsViewState
import com.audiobook.vc.features.settings.views.sleeptimer.AutoSleepTimerCard
import com.audiobook.vc.navigation.Destination
import com.audiobook.vc.navigation.NavEntryProvider
import com.audiobook.vc.core.strings.R as StringsR

@Composable
@Preview
private fun SettingsPreview() {
  VoiceTheme {
    Settings(
      SettingsViewState.preview(),
      SettingsListener.noop(),
    )
  }
}

@Composable
private fun Settings(
  viewState: SettingsViewState,
  listener: SettingsListener,
) {
  val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      TopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
          Text(stringResource(StringsR.string.action_settings))
        },
        navigationIcon = {
          IconButton(
            onClick = {
              listener.close()
            },
          ) {
            Icon(
              imageVector = Icons.Outlined.Close,
              contentDescription = stringResource(StringsR.string.close),
            )
          }
        },
      )
    },
  ) { contentPadding ->
    LazyColumn(contentPadding = contentPadding) {
      if (viewState.showFolderPickerEntry) {
        item {
          ListItem(
            modifier = Modifier.clickable { listener.openFolderPicker() },
            leadingContent = {
              Icon(
                imageVector = Icons.Outlined.Book,
                contentDescription = stringResource(StringsR.string.audiobook_folders_title),
              )
            },
            headlineContent = {
              Text(stringResource(StringsR.string.audiobook_folders_title))
            },
            supportingContent = {
              Text(stringResource(StringsR.string.pref_audiobook_folders_explanation))
            },
          )
        }
      }
      if (viewState.showDarkThemePref) {
        item {
          DarkThemeRow(viewState.useDarkTheme, listener::toggleDarkTheme)
        }
      }
      if (viewState.showAnalyticSetting) {
        item {
          AnalyticsRow(analyticsEnabled = viewState.analyticsEnabled, toggle = listener::toggleAnalytics)
        }
      }

      // Dynamic Theme (Material You)
      if (android.os.Build.VERSION.SDK_INT >= 31) {
        item {
          ListItem(
            modifier = Modifier.clickable { listener.toggleDynamicTheme() },
            leadingContent = {
              Icon(Icons.Outlined.ColorLens, contentDescription = "Dynamic Colors")
            },
            headlineContent = { Text("Dynamic Colors") },
            supportingContent = { Text("Use wallpaper colors") },
            trailingContent = {
              Switch(
                checked = viewState.useDynamicTheme,
                onCheckedChange = { listener.toggleDynamicTheme() }
              )
            }
          )
        }
      }

      item {
        ListItem(
            modifier = Modifier.clickable { listener.toggleOledTheme() },
            leadingContent = {
              Icon(Icons.Outlined.Contrast, contentDescription = "OLED Dark Mode")
            },
            headlineContent = { Text("OLED Dark Mode") },
            supportingContent = { Text("Use true black background") },
            trailingContent = {
              Switch(
                checked = viewState.useOledTheme,
                onCheckedChange = { listener.toggleOledTheme() }
              )
            }
          )
      }
      item {
        ListItem(
          modifier = Modifier.clickable { listener.toggleGrid() },
          leadingContent = {
            val imageVector = if (viewState.useGrid) {
              Icons.Outlined.GridView
            } else {
              Icons.AutoMirrored.Outlined.ViewList
            }
            Icon(imageVector, stringResource(StringsR.string.pref_use_grid))
          },
          headlineContent = { Text(stringResource(StringsR.string.pref_use_grid)) },
          trailingContent = {
            Switch(
              checked = viewState.useGrid,
              onCheckedChange = {
                listener.toggleGrid()
              },
            )
          },
        )
      }

      item {
        ListItem(
          modifier = Modifier.clickable { listener.toggleSkipSilence() },
          leadingContent = {
            Icon(
              imageVector = Icons.Outlined.GraphicEq,
              contentDescription = stringResource(StringsR.string.skip_silence),
            )
          },
          headlineContent = { Text(stringResource(StringsR.string.skip_silence)) },
          trailingContent = {
            Switch(
              checked = viewState.skipSilence,
              onCheckedChange = { listener.toggleSkipSilence() },
            )
          },
        )
      }

      item {
        SeekTimeRow(viewState.seekTimeInSeconds) {
          listener.onSeekAmountRowClick()
        }
      }

      item {
        AutoRewindRow(viewState.autoRewindInSeconds) {
          listener.onAutoRewindRowClick()
        }
      }

      item {
        AutoSleepTimerCard(viewState.autoSleepTimer, listener)
      }

      item {
        ListItem(
          modifier = Modifier.clickable { listener.suggestIdea() },
          leadingContent = {
            Icon(
              imageVector = Icons.Outlined.Lightbulb,
              contentDescription = stringResource(StringsR.string.pref_suggest_idea),
            )
          },
          headlineContent = {
            Text(stringResource(StringsR.string.pref_suggest_idea))
          },
        )
      }

      item {
        ListItem(
          modifier = Modifier.clickable { listener.getSupport() },
          leadingContent = {
            Icon(
              imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
              contentDescription = stringResource(StringsR.string.pref_get_support),
            )
          },
          headlineContent = {
            Text(stringResource(StringsR.string.pref_get_support))
          },
        )
      }

      item {
        ListItem(
          modifier = Modifier.clickable { listener.openBugReport() },
          leadingContent = {
            Icon(
              imageVector = Icons.Outlined.BugReport,
              contentDescription = stringResource(StringsR.string.pref_report_issue),
            )
          },
          headlineContent = {
            Text(stringResource(StringsR.string.pref_report_issue))
          },
        )
      }
      item {
        ListItem(
          modifier = Modifier.clickable { listener.openTranslations() },
          leadingContent = {
            Icon(
              imageVector = Icons.Outlined.Language,
              contentDescription = stringResource(StringsR.string.pref_help_translating),
            )
          },
          headlineContent = {
            Text(stringResource(StringsR.string.pref_help_translating))
          },
        )
      }
      item {
        ListItem(
          modifier = Modifier.clickable { listener.openFaq() },
          leadingContent = {
            Icon(
              imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
              contentDescription = stringResource(StringsR.string.pref_faq),
            )
          },
          headlineContent = {
            Text(stringResource(StringsR.string.pref_faq))
          },
        )
      }
      item {
        AppVersion(appVersion = viewState.appVersion)
      }
    }
    Dialog(viewState, listener)
  }
}

@Composable
private fun AnalyticsRow(
  analyticsEnabled: Boolean,
  toggle: () -> Unit,
) {
  ListItem(
    modifier = Modifier.clickable { toggle() },
    leadingContent = {
      Icon(
        imageVector = Icons.Outlined.Analytics,
        contentDescription = null,
      )
    },
    headlineContent = {
      Text(text = stringResource(StringsR.string.settings_analytics_consent_title))
    },
    supportingContent = {
      Text(text = stringResource(StringsR.string.settings_analytics_consent_description))
    },
    trailingContent = {
      Switch(
        checked = analyticsEnabled,
        onCheckedChange = { toggle() },
      )
    },
  )
}

@ContributesTo(AppScope::class)
interface SettingsGraph {
  val settingsViewModel: SettingsViewModel
}

@ContributesTo(AppScope::class)
interface SettingsProvider {

  @Provides
  @IntoSet
  fun navEntryProvider(): NavEntryProvider<*> = NavEntryProvider<Destination.Settings> { key ->
    NavEntry(key) {
      Settings()
    }
  }
}

@Composable
fun Settings() {
  val viewModel = rememberScoped { rootGraphAs<SettingsGraph>().settingsViewModel }
  val viewState = viewModel.viewState()
  Settings(viewState, viewModel)
}

@Composable
private fun Dialog(
  viewState: SettingsViewState,
  listener: SettingsListener,
) {
  val dialog = viewState.dialog ?: return
  when (dialog) {
    SettingsViewState.Dialog.AutoRewindAmount -> {
      AutoRewindAmountDialog(
        currentSeconds = viewState.autoRewindInSeconds,
        onSecondsConfirm = listener::autoRewindAmountChang,
        onDismiss = listener::dismissDialog,
      )
    }
    SettingsViewState.Dialog.SeekTime -> {
      SeekAmountDialog(
        currentSeconds = viewState.seekTimeInSeconds,
        onSecondsConfirm = listener::seekAmountChanged,
        onDismiss = listener::dismissDialog,
      )
    }
  }
}
