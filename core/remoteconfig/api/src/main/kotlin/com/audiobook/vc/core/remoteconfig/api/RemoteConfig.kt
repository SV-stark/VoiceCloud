package com.audiobook.vc.core.remoteconfig.api

interface RemoteConfig {
  fun boolean(
    key: String,
    defaultValue: Boolean = false,
  ): Boolean
  fun string(
    key: String,
    defaultValue: String,
  ): String
}
