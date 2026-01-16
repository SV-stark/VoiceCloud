package com.audiobook.vc.features.playbackScreen.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.audiobook.vc.core.playback.misc.Decibel
import com.audiobook.vc.core.strings.R

@Composable
internal fun PerBookSettingsDialog(
  playbackSpeed: Float,
  skipSilence: Boolean,
  volumeGain: Decibel,
  onDismiss: () -> Unit,
  onSpeedChange: (Float) -> Unit,
  onSkipSilenceChange: (Boolean) -> Unit,
  onVolumeGainChange: (Decibel) -> Unit,
) {
  var currentSpeed by remember { mutableFloatStateOf(playbackSpeed) }
  var currentSkipSilence by remember { mutableStateOf(skipSilence) }
  var currentGain by remember { mutableFloatStateOf(volumeGain.value) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(text = stringResource(R.string.pref_book_settings))
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        // Playback Speed
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = stringResource(R.string.playback_speed),
              style = MaterialTheme.typography.bodyMedium,
            )
            Text(
              text = "%.2fx".format(currentSpeed),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.primary,
            )
          }
          Slider(
            value = currentSpeed,
            onValueChange = {
              currentSpeed = it
              onSpeedChange(it)
            },
            valueRange = 0.5f..3.5f,
            steps = 29,
            modifier = Modifier.fillMaxWidth(),
          )
        }

        // Skip Silence
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(R.string.skip_silence),
            style = MaterialTheme.typography.bodyMedium,
          )
          Switch(
            checked = currentSkipSilence,
            onCheckedChange = {
              currentSkipSilence = it
              onSkipSilenceChange(it)
            },
          )
        }

        // Volume Gain
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = stringResource(R.string.volume_boost),
              style = MaterialTheme.typography.bodyMedium,
            )
            Text(
              text = "+%.0f dB".format(currentGain),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.primary,
            )
          }
          Slider(
            value = currentGain,
            onValueChange = {
              currentGain = it
              onVolumeGainChange(Decibel(it))
            },
            valueRange = 0f..20f,
            steps = 19,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(R.string.close))
      }
    },
  )
}
