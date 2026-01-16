package com.audiobook.vc.features.settings

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.launch
import com.audiobook.vc.core.common.AppInfoProvider
import com.audiobook.vc.core.common.DispatcherProvider
import com.audiobook.vc.core.common.MainScope
import com.audiobook.vc.core.data.GridMode
import com.audiobook.vc.core.data.sleeptimer.SleepTimerPreference
import com.audiobook.vc.core.data.store.AnalyticsConsentStore
import com.audiobook.vc.core.data.store.AutoRewindAmountStore
import com.audiobook.vc.core.data.store.DarkThemeStore
import com.audiobook.vc.core.data.store.DynamicThemeStore
import com.audiobook.vc.core.data.store.OledThemeStore
import com.audiobook.vc.core.data.store.GridModeStore
import com.audiobook.vc.core.data.store.SeekTimeStore
import com.audiobook.vc.core.data.store.SkipSilenceStore
import com.audiobook.vc.core.data.store.SleepTimerPreferenceStore
import com.audiobook.vc.core.featureflag.FeatureFlag
import com.audiobook.vc.core.featureflag.FolderPickerInSettingsFeatureFlagQualifier
import com.audiobook.vc.core.ui.DARK_THEME_SETTABLE
import com.audiobook.vc.core.ui.GridCount
import com.audiobook.vc.navigation.Destination
import com.audiobook.vc.navigation.Navigator
import java.time.LocalTime

@Inject
class SettingsViewModel(
  @DarkThemeStore
  private val useDarkThemeStore: DataStore<Boolean>,
  @AutoRewindAmountStore
  private val autoRewindAmountStore: DataStore<Int>,
  @SeekTimeStore
  private val seekTimeStore: DataStore<Int>,
  private val navigator: Navigator,
  private val appInfoProvider: AppInfoProvider,
  @GridModeStore
  private val gridModeStore: DataStore<GridMode>,
  @SleepTimerPreferenceStore
  private val sleepTimerPreferenceStore: DataStore<SleepTimerPreference>,
  @AnalyticsConsentStore
  private val analyticsConsentStore: DataStore<Boolean>,
  @OledThemeStore
  private val oledThemeStore: DataStore<Boolean>,
  @DynamicThemeStore
  private val dynamicThemeStore: DataStore<Boolean>,
  @SkipSilenceStore
  private val skipSilenceStore: DataStore<Boolean>,
  private val gridCount: GridCount,
  @FolderPickerInSettingsFeatureFlagQualifier
  private val folderPickerInSettingsFeatureFlag: FeatureFlag<Boolean>,
  dispatcherProvider: DispatcherProvider,
) : SettingsListener {

  private val mainScope = MainScope(dispatcherProvider)
  private val dialog = mutableStateOf<SettingsViewState.Dialog?>(null)

  @Composable
  fun viewState(): SettingsViewState {
    val useDarkTheme by remember { useDarkThemeStore.data }.collectAsState(initial = false)
    val useOledTheme by remember { oledThemeStore.data }.collectAsState(initial = false)
    val useDynamicTheme by remember { dynamicThemeStore.data }.collectAsState(initial = true)
    
    val autoRewindAmount by remember { autoRewindAmountStore.data }.collectAsState(initial = 0)
    val seekTime by remember { seekTimeStore.data }.collectAsState(initial = 0)
    val gridMode by remember { gridModeStore.data }.collectAsState(initial = GridMode.GRID)
    val autoSleepTimer by remember { sleepTimerPreferenceStore.data }.collectAsState(
      initial = SleepTimerPreference.Default,
    )
    val analyticsEnabled by remember { analyticsConsentStore.data }.collectAsState(initial = false)
    val showFolderPickerEntry = remember {
      folderPickerInSettingsFeatureFlag.get()
    }
    val skipSilence by remember { skipSilenceStore.data }.collectAsState(initial = false)
    return SettingsViewState(
      useDarkTheme = useDarkTheme,
      useOledTheme = useOledTheme,
      useDynamicTheme = useDynamicTheme,
      showDarkThemePref = DARK_THEME_SETTABLE,
      seekTimeInSeconds = seekTime,
      autoRewindInSeconds = autoRewindAmount,
      dialog = dialog.value,
      appVersion = appInfoProvider.versionName,
      useGrid = when (gridMode) {
        GridMode.LIST -> false
        GridMode.GRID -> true
        GridMode.FOLLOW_DEVICE -> gridCount.useGridAsDefault()
      },
      autoSleepTimer = SettingsViewState.AutoSleepTimerViewState(
        enabled = autoSleepTimer.autoSleepTimerEnabled,
        startTime = autoSleepTimer.autoSleepStartTime,
        endTime = autoSleepTimer.autoSleepEndTime,
      ),
      analyticsEnabled = analyticsEnabled,
      showAnalyticSetting = appInfoProvider.analyticsIncluded,
      showFolderPickerEntry = showFolderPickerEntry,
      skipSilence = skipSilence,
    )
  }

  override fun close() {
    navigator.goBack()
  }

  override fun toggleDarkTheme() {
    mainScope.launch {
      useDarkThemeStore.updateData { !it }
    }
  }

  override fun toggleGrid() {
    mainScope.launch {
      gridModeStore.updateData { currentMode ->
        when (currentMode) {
          GridMode.LIST -> GridMode.GRID
          GridMode.GRID -> GridMode.LIST
          GridMode.FOLLOW_DEVICE -> if (gridCount.useGridAsDefault()) {
            GridMode.LIST
          } else {
            GridMode.GRID
          }
        }
      }
    }
  }

  override fun seekAmountChanged(seconds: Int) {
    mainScope.launch {
      seekTimeStore.updateData { seconds }
    }
  }

  override fun onSeekAmountRowClick() {
    dialog.value = SettingsViewState.Dialog.SeekTime
  }

  override fun autoRewindAmountChang(seconds: Int) {
    mainScope.launch {
      autoRewindAmountStore.updateData { seconds }
    }
  }

  override fun onAutoRewindRowClick() {
    dialog.value = SettingsViewState.Dialog.AutoRewindAmount
  }

  override fun dismissDialog() {
    dialog.value = null
  }

  override fun getSupport() {
    navigator.goTo(Destination.Website("https://github.com/SV-stark/VoiceCloud/discussions"))
  }

  override fun suggestIdea() {
    navigator.goTo(Destination.Website("https://github.com/SV-stark/VoiceCloud/discussions"))
  }

  override fun openBugReport() {
    val url = "https://github.com/SV-stark/VoiceCloud/issues/new".toUri()
      .buildUpon()
      .appendQueryParameter("template", "bug.yml")
      .appendQueryParameter("version", appInfoProvider.versionName)
      .appendQueryParameter("androidversion", Build.VERSION.SDK_INT.toString())
      .appendQueryParameter("device", Build.MODEL)
      .toString()
    navigator.goTo(Destination.Website(url))
  }

  override fun openTranslations() {
    dismissDialog()
    navigator.goTo(Destination.Website("https://hosted.weblate.org/engage/voice/"))
  }

  override fun openFaq() {
    navigator.goTo(Destination.Website("https://github.com/SV-stark/VoiceCloud/wiki"))
  }

  override fun openFolderPicker() {
    navigator.goTo(Destination.FolderPicker)
  }

  override fun setAutoSleepTimer(checked: Boolean) {
    mainScope.launch {
      sleepTimerPreferenceStore.updateData { currentPrefs ->
        currentPrefs.copy(autoSleepTimerEnabled = checked)
      }
    }
  }

  override fun setAutoSleepTimerStart(time: LocalTime) {
    mainScope.launch {
      sleepTimerPreferenceStore.updateData { currentPrefs ->
        currentPrefs.copy(autoSleepStartTime = time)
      }
    }
  }

  override fun setAutoSleepTimerEnd(time: LocalTime) {
    mainScope.launch {
      sleepTimerPreferenceStore.updateData { currentPrefs ->
        currentPrefs.copy(autoSleepEndTime = time)
      }
    }
  }

  override fun toggleAnalytics() {
    mainScope.launch {
      analyticsConsentStore.updateData { !it }
    }
  }

  override fun toggleOledTheme() {
    mainScope.launch {
      oledThemeStore.updateData { !it }
    }
  }

  override fun toggleDynamicTheme() {
    mainScope.launch {
      dynamicThemeStore.updateData { !it }
    }
  }

  override fun toggleSkipSilence() {
    mainScope.launch {
      skipSilenceStore.updateData { !it }
    }
  }
}
