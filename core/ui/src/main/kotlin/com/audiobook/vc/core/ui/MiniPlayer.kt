package com.audiobook.vc.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.io.File

@Composable
fun MiniPlayer(
  bookTitle: String,
  chapterName: String?,
  coverFile: File?,
  isPlaying: Boolean,
  progress: Float,
  onPlayPause: () -> Unit,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    tonalElevation = 3.dp,
    shadowElevation = 4.dp,
  ) {
    Column {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp)
          .clickable { onClick() }
          .padding(horizontal = 8.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.matchParentSize(),
        ) {
          // Cover thumbnail
          AsyncImage(
            model = coverFile,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.album_art),
            error = painterResource(id = R.drawable.album_art),
            modifier = Modifier
              .size(44.dp)
              .clip(RoundedCornerShape(8.dp)),
          )
          
          Spacer(Modifier.width(12.dp))
          
          // Title and chapter
          Column(
            modifier = Modifier.weight(1f),
          ) {
            Text(
              text = bookTitle,
              style = MaterialTheme.typography.titleSmall,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            if (chapterName != null) {
              Text(
                text = chapterName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
          
          // Play/Pause button
          IconButton(onClick = onPlayPause) {
            Icon(
              imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
              contentDescription = if (isPlaying) "Pause" else "Play",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(32.dp),
            )
          }
        }
      }
      
      // Progress bar at bottom
      LinearProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        modifier = Modifier
          .fillMaxWidth()
          .height(2.dp),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
      )
    }
  }
}
