package com.audiobook.vc.core.ui

import androidx.datastore.core.DataStore
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import com.audiobook.vc.core.data.store.DarkThemeStore
import com.audiobook.vc.core.data.store.DynamicThemeStore
import com.audiobook.vc.core.data.store.OledThemeStore

@ContributesTo(AppScope::class)
interface SharedGraph {

  @get:DarkThemeStore
  val useDarkThemeStore: DataStore<Boolean>

  @get:OledThemeStore
  val useOledThemeStore: DataStore<Boolean>

  @get:DynamicThemeStore
  val useDynamicThemeStore: DataStore<Boolean>
}
