package com.audiobook.vc.app.di

import android.app.Application
import android.content.Context
import android.os.PowerManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import com.audiobook.vc.app.misc.AppInfoProviderImpl
import com.audiobook.vc.app.misc.MainActivityIntentProviderImpl
import com.audiobook.vc.core.common.AppInfoProvider
import com.audiobook.vc.core.common.DispatcherProvider
import com.audiobook.vc.core.common.MainScope
import com.audiobook.vc.core.playback.notification.MainActivityIntentProvider
import java.time.Clock

/**
 * Module providing Android SDK Related instances.
 */
@BindingContainer
@ContributesTo(AppScope::class)
object AndroidModule {

  @Provides
  fun provideContext(app: Application): Context = app

  @Provides
  fun coroutineScope(dispatcherProvider: DispatcherProvider): CoroutineScope = MainScope(dispatcherProvider)

  @Provides
  @SingleIn(AppScope::class)
  fun providePowerManager(context: Context): PowerManager {
    return context.getSystemService(Context.POWER_SERVICE) as PowerManager
  }

  @Provides
  fun toToBookIntentProvider(impl: MainActivityIntentProviderImpl): MainActivityIntentProvider = impl

  @Provides
  fun applicationIdProvider(impl: AppInfoProviderImpl): AppInfoProvider = impl

  @Provides
  @SingleIn(AppScope::class)
  fun json(): Json {
    return Json.Default
  }

  @Provides
  @SingleIn(AppScope::class)
  fun dispatcherProvider(): DispatcherProvider {
    return DispatcherProvider()
  }

  @Provides
  fun clock(): Clock = Clock.systemDefaultZone()
}
