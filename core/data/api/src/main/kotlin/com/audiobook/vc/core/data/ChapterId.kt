package com.audiobook.vc.core.data

import android.net.Uri
import androidx.core.net.toUri
import kotlinx.serialization.Serializable
import com.audiobook.vc.core.common.comparator.NaturalOrderComparator

@Serializable(with = ChapterIdSerializer::class)
public data class ChapterId(val value: String) : Comparable<ChapterId> {
  public constructor(uri: Uri) : this(uri.toString())

  override fun compareTo(other: ChapterId): Int {
    return NaturalOrderComparator.uriComparator.compare(value.toUri(), other.value.toUri())
  }
}
