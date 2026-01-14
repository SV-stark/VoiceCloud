package com.audiobook.vc.features.onboarding.explanation

import androidx.navigation3.runtime.NavEntry
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import com.audiobook.vc.navigation.Destination
import com.audiobook.vc.navigation.NavEntryProvider

@ContributesTo(AppScope::class)
interface OnboardingExplanationProvider {

  val onboardingExplanationViewModel: OnboardingExplanationViewModel

  @Provides
  @IntoSet
  fun navEntryProvider(): NavEntryProvider<*> = NavEntryProvider<Destination.OnboardingExplanation> { key ->
    NavEntry(key) {
      OnboardingExplanation()
    }
  }
}
