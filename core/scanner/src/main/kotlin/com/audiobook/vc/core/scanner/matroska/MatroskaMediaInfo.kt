package com.audiobook.vc.core.scanner.matroska

import com.audiobook.vc.core.data.MarkData

internal data class MatroskaMediaInfo(
  val album: String? = null,
  val artist: String? = null,
  val title: String? = null,
  val chapters: List<MarkData> = emptyList(),
)
