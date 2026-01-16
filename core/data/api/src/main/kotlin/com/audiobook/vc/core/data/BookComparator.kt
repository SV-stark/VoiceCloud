package com.audiobook.vc.core.data

import com.audiobook.vc.core.common.comparator.NaturalOrderComparator

public enum class BookComparator(private val comparatorFunction: Comparator<Book>) : Comparator<Book> by comparatorFunction {

  ByLastPlayed(
    compareByDescending {
      it.content.lastPlayedAt
    },
  ),
  ByName(
    Comparator { left, right ->
      NaturalOrderComparator.stringComparator.compare(left.content.name, right.content.name)
    },
  ),
  BySeries(
    compareBy(
      { it.content.series ?: "" },
      {
        it.content.part?.let { part ->
          try {
            // Try to parse as float to handle 1.5, etc.
            part.toFloat()
          } catch (e: NumberFormatException) {
            // Fallback for non-numeric parts, use max value to put at end?
            // Or just try to parse int?
            // Let's rely on string comparison if not numeric?
            // Existing impl uses NaturalOrderComparator for names.
            // Let's just use part as string with NaturalOrderComparator if possible?
            // But NaturalOrderComparator is java.util.Comparator.
            // Let's stick to the plan: try to parse Float/Int
            Float.MAX_VALUE
          }
        } ?: Float.MAX_VALUE
      },
    ),
  ),
}
