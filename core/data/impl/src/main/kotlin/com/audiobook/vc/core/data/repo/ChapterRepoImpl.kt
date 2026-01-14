package com.audiobook.vc.core.data.repo

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import com.audiobook.vc.core.data.Chapter
import com.audiobook.vc.core.data.ChapterId
import com.audiobook.vc.core.data.repo.internals.dao.ChapterDao
import com.audiobook.vc.core.data.runForMaxSqlVariableNumber

@Inject
@ContributesBinding(AppScope::class)
public class ChapterRepoImpl(private val dao: ChapterDao) : ChapterRepo {

  private val cache = mutableMapOf<ChapterId, Chapter?>()

  override suspend fun get(id: ChapterId): Chapter? {
    // this does not use getOrPut because a `null` value should also be cached
    if (!cache.containsKey(id)) {
      cache[id] = dao.chapter(id)
    }
    return cache[id]
  }

  internal suspend fun warmup(ids: List<ChapterId>) {
    val missing = ids.filter { it !in cache }
    missing
      .runForMaxSqlVariableNumber {
        dao.chapters(it)
      }
      .forEach { cache[it.id] = it }
  }

  override suspend fun put(chapter: Chapter) {
    dao.insert(chapter)
    cache[chapter.id] = chapter
  }
}
