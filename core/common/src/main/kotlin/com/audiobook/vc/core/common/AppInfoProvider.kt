package com.audiobook.vc.core.common

interface AppInfoProvider {
  val versionName: String

  val analyticsIncluded: Boolean
}
