package com.audiobook.vc.features.review

import android.content.Context
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.Qualifier
import dev.zacsweers.metro.SingleIn
import com.audiobook.vc.core.featureflag.FeatureFlag
import com.audiobook.vc.core.featureflag.FeatureFlagFactory

@ContributesTo(AppScope::class)
@BindingContainer
object ReviewModule {

  @Provides
  fun reviewManager(context: Context): ReviewManager {
    return ReviewManagerFactory.create(context)
  }

  @Provides
  @SingleIn(AppScope::class)
  @ReviewEnabledFeatureFlagQualifier
  fun reviewEnabledFeatureFlag(factory: FeatureFlagFactory): FeatureFlag<Boolean> = factory.boolean("review_enabled")
}

@Qualifier
annotation class ReviewEnabledFeatureFlagQualifier
