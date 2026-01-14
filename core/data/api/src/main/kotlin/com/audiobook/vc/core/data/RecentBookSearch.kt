package com.audiobook.vc.core.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recentBookSearch")
public data class RecentBookSearch(
  @PrimaryKey
  val searchTerm: String,
)
