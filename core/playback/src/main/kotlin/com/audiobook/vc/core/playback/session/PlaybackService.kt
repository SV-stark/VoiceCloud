package com.audiobook.vc.core.playback.session

import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import com.audiobook.vc.core.common.rootGraphAs
import com.audiobook.vc.core.logging.api.Logger
import com.audiobook.vc.core.playback.di.PlaybackGraph
import com.audiobook.vc.core.playback.player.VoicePlayer

class PlaybackService : MediaLibraryService() {

  @Inject
  lateinit var session: MediaLibrarySession

  @Inject
  lateinit var scope: CoroutineScope

  @Inject
  lateinit var player: VoicePlayer

  @Inject
  lateinit var voiceNotificationProvider: VoiceMediaNotificationProvider

  override fun onCreate() {
    super.onCreate()
    rootGraphAs<PlaybackGraph.Provider>()
      .playbackGraphFactory
      .create(this)
      .inject(this)
    setMediaNotificationProvider(voiceNotificationProvider)
  }

  private fun release() {
    player.release()
    session.release()
    scope.cancel()
  }

  override fun onDestroy() {
    super.onDestroy()
    release()
  }

  override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
    return session.takeUnless { session ->
      session.invokeIsReleased
    }.also {
      if (it == null) {
        Logger.w("onGetSession returns null because the session is already released")
      }
    }
  }
}

private val MediaSession.invokeIsReleased: Boolean
  get() = try {
    // temporarily checked to debug
    // https://github.com/androidx/media/issues/422
    MediaSession::class.java.getDeclaredMethod("isReleased")
      .apply { isAccessible = true }
      .invoke(this) as Boolean
  } catch (e: Exception) {
    Logger.w(e, "Couldn't check if it's released")
    false
  }
