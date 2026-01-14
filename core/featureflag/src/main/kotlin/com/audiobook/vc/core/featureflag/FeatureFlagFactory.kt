package com.audiobook.vc.core.featureflag

import dev.zacsweers.metro.Inject
import com.audiobook.vc.core.remoteconfig.api.RemoteConfig

@Inject
class FeatureFlagFactory(private val remoteConfig: RemoteConfig) {

  fun boolean(
    key: String,
    defaultValue: Boolean = false,
  ): FeatureFlag<Boolean> {
    return RemoteConfigFeatureFlag(remoteConfig = remoteConfig) {
      it.boolean(key = key, defaultValue = defaultValue)
    }
  }

  fun string(
    key: String,
    defaultValue: String,
  ): FeatureFlag<String> {
    return RemoteConfigFeatureFlag(remoteConfig = remoteConfig) {
      it.string(key = key, defaultValue = defaultValue)
    }
  }
}
