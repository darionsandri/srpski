package com.srpski.app.data

import com.srpski.app.data.db.InterestDao
import com.srpski.app.data.db.InterestWeight
import com.srpski.app.data.model.TopicMap
import com.srpski.app.domain.InterestUpdater
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Bridges the InterestDao and the pure [InterestUpdater] logic.
 * Also handles first-run seeding from a topic list picked during onboarding.
 */
class InterestRepository(
    private val dao: InterestDao,
    private val clock: () -> Long = { System.currentTimeMillis() / 1000L }
) {
    fun observeAll(): Flow<Map<String, Double>> =
        dao.all().map { list -> list.associate { it.topicId to it.weight } }

    suspend fun snapshot(): Map<String, Double> =
        dao.snapshot().associate { it.topicId to it.weight }

    suspend fun hasSeeded(): Boolean = dao.count() > 0

    /** Seed weights the first time a user finishes onboarding. Idempotent when called twice. */
    suspend fun seed(topicMap: TopicMap, pickedTopicIds: Set<String>) {
        val now = clock()
        val rows = topicMap.topics.map { t ->
            val w = if (t.id in pickedTopicIds) InterestUpdater.pickedSeed() else InterestUpdater.unpickedSeed()
            InterestWeight(topicId = t.id, weight = w, updatedAtEpochSec = now)
        }
        dao.upsertAll(rows)
    }

    /** Called by the share ingest pipeline for each topic detected in a shared item. */
    suspend fun reinforce(topicIds: Collection<String>) {
        if (topicIds.isEmpty()) return
        val now = clock()
        val unique = topicIds.toSet()
        for (topicId in unique) {
            val current = dao.byTopic(topicId)?.weight ?: InterestUpdater.MIN_WEIGHT
            val updated = InterestUpdater.reinforce(current)
            dao.upsert(InterestWeight(topicId, updated, now))
        }
    }

    /** Weekly job (WorkManager, M7) walks every topic and decays. */
    suspend fun decayAll() {
        val now = clock()
        for (row in dao.snapshot()) {
            val updated = InterestUpdater.decay(row.weight)
            if (updated != row.weight) dao.upsert(row.copy(weight = updated, updatedAtEpochSec = now))
        }
    }
}
