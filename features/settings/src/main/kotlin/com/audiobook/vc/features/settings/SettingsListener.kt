package com.audiobook.vc.features.settings

import java.time.LocalTime

interface SettingsListener {
  fun close()
  fun toggleDarkTheme()
  fun toggleGrid()
  fun seekAmountChanged(seconds: Int)
  fun onSeekAmountRowClick()
  fun autoRewindAmountChang(seconds: Int)
  fun onAutoRewindRowClick()
  fun dismissDialog()
  fun getSupport()
  fun suggestIdea()
  fun openBugReport()
  fun openTranslations()
  fun openFaq()
  fun setAutoSleepTimer(checked: Boolean)
  fun setAutoSleepTimerStart(time: LocalTime)
  fun setAutoSleepTimerEnd(time: LocalTime)
  fun toggleAnalytics()
  fun toggleOledTheme()
  fun toggleDynamicTheme()
  fun toggleSkipSilence()
  fun openFolderPicker()

  companion object {
    fun noop() = object : SettingsListener {
      override fun close() {}
      override fun toggleDarkTheme() {}
      override fun toggleGrid() {}
      override fun seekAmountChanged(seconds: Int) {}
      override fun onSeekAmountRowClick() {}
      override fun autoRewindAmountChang(seconds: Int) {}
      override fun onAutoRewindRowClick() {}
      override fun dismissDialog() {}
      override fun getSupport() {}
      override fun suggestIdea() {}
      override fun openBugReport() {}
      override fun openTranslations() {}
      override fun openFaq() {}
      override fun setAutoSleepTimer(checked: Boolean) {}
      override fun setAutoSleepTimerStart(time: LocalTime) {}
      override fun setAutoSleepTimerEnd(time: LocalTime) {}
      override fun toggleAnalytics() {}
      override fun toggleOledTheme() {}
      override fun toggleDynamicTheme() {}
      override fun toggleSkipSilence() {}
      override fun openFolderPicker() {}
    }
  }
}
