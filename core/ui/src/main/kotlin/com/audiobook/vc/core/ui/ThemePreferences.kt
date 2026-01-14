package com.audiobook.vc.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import kotlinx.coroutines.Dispatchers
import com.audiobook.vc.core.common.rootGraphAs

/**
 * Returns true if the user has enabled the OLED (True Black) theme preference.
 */
@Composable
fun isOledTheme(): Boolean {
  val oledThemeFlow = remember {
    rootGraphAs<SharedGraph>().useOledThemeStore.data
  }
  return oledThemeFlow.collectAsState(initial = false, context = Dispatchers.Unconfined).value
}

/**
 * Returns true if the user has enabled the Dynamic Theme (Material You) preference.
 * Defaults to true.
 */
@Composable
fun isDynamicTheme(): Boolean {
  val dynamicThemeFlow = remember {
    rootGraphAs<SharedGraph>().useDynamicThemeStore.data
  }
  return dynamicThemeFlow.collectAsState(initial = true, context = Dispatchers.Unconfined).value
}
