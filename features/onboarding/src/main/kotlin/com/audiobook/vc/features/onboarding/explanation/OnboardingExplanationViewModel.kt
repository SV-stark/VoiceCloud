package com.audiobook.vc.features.onboarding.explanation

import androidx.datastore.core.DataStore
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import com.audiobook.vc.core.common.AppInfoProvider
import com.audiobook.vc.core.data.store.AnalyticsConsentStore
import com.audiobook.vc.navigation.Destination
import com.audiobook.vc.navigation.Navigator
import com.audiobook.vc.navigation.Origin

@Inject
class OnboardingExplanationViewModel(
  private val navigator: Navigator,
  @AnalyticsConsentStore
  private val analyticsConsentStore: DataStore<Boolean>,
  private val appInfoProvider: AppInfoProvider,
) {

  private val scope = MainScope()

  fun viewState(): OnboardingExplanationViewState {
    return OnboardingExplanationViewState(
      askForAnalytics = appInfoProvider.analyticsIncluded,
    )
  }

  fun onContinueWithAnalytics() {
    scope.launch {
      analyticsConsentStore.updateData { true }
    }
    navigator.goTo(Destination.AddContent(origin = Origin.Onboarding))
  }

  fun onContinueWithoutAnalytics() {
    scope.launch {
      analyticsConsentStore.updateData { false }
    }
    navigator.goTo(Destination.AddContent(origin = Origin.Onboarding))
  }

  fun onPrivacyPolicyClick() {
    navigator.goTo(Destination.Website("https://voice.woitaschek.de/privacy-policy"))
  }

  fun onClose() {
    navigator.goBack()
  }
}
