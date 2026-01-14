package com.audiobook.vc.features.onboarding.welcome

import dev.zacsweers.metro.Inject
import com.audiobook.vc.navigation.Destination
import com.audiobook.vc.navigation.Navigator

@Inject
class OnboardingWelcomeViewModel(private val navigator: Navigator) {

  fun next() {
    navigator.goTo(Destination.OnboardingExplanation)
  }
}
