package com.audiobook.vc.features.playbackScreen.view

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.audiobook.vc.core.ui.ImmutableFile
import com.audiobook.vc.core.strings.R as StringsR
import com.audiobook.vc.core.ui.R as UiR

@Composable
internal fun Cover(
  onPlayClick: () -> Unit,
  onRewindClick: () -> Unit,
  onFastForwardClick: () -> Unit,
  onSkipToPrevious: () -> Unit,
  onSkipToNext: () -> Unit,
  cover: ImmutableFile?,
) {
  val swipeThreshold = 100f
  var totalDrag by remember { mutableFloatStateOf(0f) }
  
  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .pointerInput(Unit) {
        detectHorizontalDragGestures(
          onDragEnd = {
            if (totalDrag > swipeThreshold) {
              onSkipToPrevious()
            } else if (totalDrag < -swipeThreshold) {
              onSkipToNext()
            }
            totalDrag = 0f
          },
          onDragCancel = {
            totalDrag = 0f
          },
          onHorizontalDrag = { _, dragAmount ->
            totalDrag += dragAmount
          },
        )
      }
      .pointerInput(Unit) {
        detectTapGestures(
          onDoubleTap = { offset ->
            val isLeftSide = offset.x < size.width / 2
            if (isLeftSide) {
              onRewindClick()
            } else {
              onFastForwardClick()
            }
          },
          onTap = {
            onPlayClick()
          },
        )
      }
      .clip(RoundedCornerShape(20.dp)),
  ) {
    AsyncImage(
      modifier = Modifier.fillMaxSize(),
      contentScale = ContentScale.Crop,
      model = cover?.file,
      placeholder = painterResource(id = UiR.drawable.album_art),
      error = painterResource(id = UiR.drawable.album_art),
      contentDescription = stringResource(id = StringsR.string.cover),
    )
  }
}
