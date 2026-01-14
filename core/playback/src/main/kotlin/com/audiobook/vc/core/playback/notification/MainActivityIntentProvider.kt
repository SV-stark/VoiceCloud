package com.audiobook.vc.core.playback.notification

import android.app.PendingIntent

interface MainActivityIntentProvider {
  fun toCurrentBook(): PendingIntent
}
