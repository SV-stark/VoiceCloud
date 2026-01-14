package com.audiobook.vc.core.logging.crashlytics

import android.app.Application
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import com.audiobook.vc.core.initializer.AppInitializer
import com.audiobook.vc.core.logging.api.Logger

@ContributesIntoSet(AppScope::class)
@Inject
class CrashlyticsLogWriterInitializer : AppInitializer {

  override fun onAppStart(application: Application) {
    Logger.install(CrashlyticsLogWriter())
  }
}
