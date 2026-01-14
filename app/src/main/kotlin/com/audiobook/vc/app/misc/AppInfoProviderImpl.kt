package com.audiobook.vc.app.misc

import dev.zacsweers.metro.Inject
import com.audiobook.vc.BuildConfig
import com.audiobook.vc.core.common.AppInfoProvider

@Inject
class AppInfoProviderImpl : AppInfoProvider {
  override val versionName: String = BuildConfig.VERSION_NAME
  override val analyticsIncluded: Boolean = BuildConfig.INCLUDE_ANALYTICS
}
