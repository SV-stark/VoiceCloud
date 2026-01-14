package com.audiobook.vc.core.ui

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun VoiceTheme(content: @Composable () -> Unit) {
  MaterialTheme(
  val isDark = isDarkTheme()
  val isDynamic = isDynamicTheme()
  val isOled = isOledTheme()
  
  val colorScheme = when {
    isDynamic && Build.VERSION.SDK_INT >= 31 -> {
      val context = LocalContext.current
      if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    isDark -> darkColorScheme()
    else -> lightColorScheme()
  }

  val finalColorScheme = if (isDark && isOled) {
    colorScheme.copy(
      background = androidx.compose.ui.graphics.Color.Black,
      surface = androidx.compose.ui.graphics.Color.Black,
    )
  } else {
    colorScheme
  }

  MaterialTheme(
    colorScheme = finalColorScheme,
    shapes = Shapes,
    typography = Typography,
  ) {
    content()
  }
}
