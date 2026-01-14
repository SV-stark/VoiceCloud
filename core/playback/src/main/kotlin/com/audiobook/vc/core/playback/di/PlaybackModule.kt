package com.audiobook.vc.core.playback.di

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaLibraryService
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import com.audiobook.vc.core.googledrive.CompositeDataSourceFactory
import com.audiobook.vc.core.googledrive.GoogleDriveDataSourceFactory
import com.audiobook.vc.core.playback.misc.VolumeGain
import com.audiobook.vc.core.playback.notification.MainActivityIntentProvider
import com.audiobook.vc.core.playback.player.DurationInconsistenciesUpdater
import com.audiobook.vc.core.playback.player.OnlyAudioRenderersFactory
import com.audiobook.vc.core.playback.player.VoicePlayer
import com.audiobook.vc.core.playback.player.onAudioSessionIdChanged
import com.audiobook.vc.core.playback.playstate.PlayStateDelegatingListener
import com.audiobook.vc.core.playback.playstate.PositionUpdater
import com.audiobook.vc.core.playback.session.LibrarySessionCallback
import com.audiobook.vc.core.playback.session.PlaybackService
import com.audiobook.vc.core.strings.R as StringsR

@BindingContainer
@ContributesTo(PlaybackScope::class)
open class PlaybackModule {

  @Provides
  @PlaybackScope
  @UnstableApi
  fun mediaSourceFactory(
    context: Context,
    googleDriveDataSourceFactory: GoogleDriveDataSourceFactory,
  ): MediaSource.Factory {
    val localDataSourceFactory = DefaultDataSource.Factory(context)
    val compositeDataSourceFactory = CompositeDataSourceFactory(
      localDataSourceFactory,
      googleDriveDataSourceFactory,
    )
    val extractorsFactory = DefaultExtractorsFactory()
      .setConstantBitrateSeekingEnabled(true)
    return ProgressiveMediaSource.Factory(compositeDataSourceFactory, extractorsFactory)
  }

  @Provides
  @PlaybackScope
  fun player(
    context: Context,
    onlyAudioRenderersFactory: OnlyAudioRenderersFactory,
    mediaSourceFactory: MediaSource.Factory,
    playStateDelegatingListener: PlayStateDelegatingListener,
    positionUpdater: PositionUpdater,
    volumeGain: VolumeGain,
    durationInconsistenciesUpdater: DurationInconsistenciesUpdater,
  ): Player {
    val audioAttributes = AudioAttributes.Builder()
      .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
      .setUsage(C.USAGE_MEDIA)
      .build()

    return ExoPlayer.Builder(context, onlyAudioRenderersFactory, mediaSourceFactory)
      .setAudioAttributes(audioAttributes, true)
      .setHandleAudioBecomingNoisy(true)
      .setWakeMode(C.WAKE_MODE_LOCAL)
      .build()
      .also { player ->
        playStateDelegatingListener.attachTo(player)
        positionUpdater.attachTo(player)
        durationInconsistenciesUpdater.attachTo(player)
        player.onAudioSessionIdChanged {
          volumeGain.audioSessionId = it
        }
      }
  }

  @Provides
  @PlaybackScope
  fun scope(): CoroutineScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

  @Provides
  @PlaybackScope
  fun session(
    service: PlaybackService,
    player: VoicePlayer,
    callback: LibrarySessionCallback,
    mainActivityIntentProvider: MainActivityIntentProvider,
    context: Context,
  ): MediaLibraryService.MediaLibrarySession {
    return MediaLibraryService.MediaLibrarySession.Builder(service, player, callback)
      .setSessionActivity(mainActivityIntentProvider.toCurrentBook())
      .setMediaButtonPreferences(
        listOf(
          CommandButton.Builder(CommandButton.ICON_SKIP_BACK)
            .setDisplayName(context.getString(StringsR.string.rewind))
            .setPlayerCommand(Player.COMMAND_SEEK_BACK)
            .setSlots(CommandButton.SLOT_BACK)
            .build(),
          CommandButton.Builder(CommandButton.ICON_SKIP_FORWARD)
            .setDisplayName(context.getString(StringsR.string.fast_forward))
            .setPlayerCommand(Player.COMMAND_SEEK_FORWARD)
            .setSlots(CommandButton.SLOT_FORWARD)
            .build(),
        ),
      )
      .build()
  }
}
