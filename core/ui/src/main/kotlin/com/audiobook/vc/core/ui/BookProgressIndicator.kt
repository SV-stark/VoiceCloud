package com.audiobook.vc.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun BookProgressIndicator(
  progress: Float,
  modifier: Modifier = Modifier,
  strokeWidth: Dp = 4.dp,
  color: Color = MaterialTheme.colorScheme.primary,
  trackColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
) {
  Canvas(modifier = modifier) {
    val diameter = size.minDimension
    val strokeWidthPx = strokeWidth.toPx()
    val radius = (diameter - strokeWidthPx) / 2f
    
    val topLeft = Offset(
      x = (size.width - diameter) / 2f + strokeWidthPx / 2f,
      y = (size.height - diameter) / 2f + strokeWidthPx / 2f
    )
    val sizePx = Size(diameter - strokeWidthPx, diameter - strokeWidthPx)

    // Track
    drawArc(
      color = trackColor,
      startAngle = 0f,
      sweepAngle = 360f,
      useCenter = false,
      topLeft = topLeft,
      size = sizePx,
      style = Stroke(width = strokeWidthPx)
    )

    // Progress
    drawArc(
      color = color,
      startAngle = -90f,
      sweepAngle = 360f * progress,
      useCenter = false,
      topLeft = topLeft,
      size = sizePx,
      style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
    )
  }
}
