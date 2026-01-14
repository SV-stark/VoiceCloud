package com.audiobook.vc.core.scanner

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import com.audiobook.vc.core.initializer.AppInitializer

@ContributesIntoSet(AppScope::class)
@Inject
public class MediaScanInitializer(private val mediaScanTrigger: MediaScanTrigger) : AppInitializer {

  override fun onAppStart(application: android.app.Application) {
    mediaScanTrigger.scan()
  }
}
