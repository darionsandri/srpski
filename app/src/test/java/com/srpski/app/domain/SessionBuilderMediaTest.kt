package com.srpski.app.domain

import com.srpski.app.data.db.SharedMedium
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SessionBuilderMediaTest {

    private fun medium(id: String, tags: String) = SharedMedium(
        id = id,
        url = "u",
        kind = SharedMedium.KIND_YOUTUBE,
        title = null,
        tagsCsv = tags,
        sharedAtEpochSec = 0L,
        status = SharedMedium.STATUS_ENRICHED
    )

    @Test fun emptyInputReturnsEmpty() {
        assertEquals(emptyList<SharedMedium>(), SessionBuilder.sampleByInterest(emptyList(), emptyMap()))
    }

    @Test fun mediumWithoutMatchingWeightExcluded() {
        val result = SessionBuilder.sampleByInterest(
            media = listOf(medium("a", "sport")),
            weights = mapOf("music" to 1.0),
            take = 1
        )
        assertTrue(result.isEmpty())
    }

    @Test fun higherWeightItemDominatesSamplingProbability() {
        val hot = medium("hot", "music")
        val cold = medium("cold", "sport")
        val counts = HashMap<String, Int>()
        repeat(1000) {
            val picks = SessionBuilder.sampleByInterest(
                media = listOf(hot, cold),
                weights = mapOf("music" to 1.0, "sport" to 0.05),
                take = 1,
                rng = Random(it.toLong())
            )
            picks.firstOrNull()?.let { m -> counts[m.id] = (counts[m.id] ?: 0) + 1 }
        }
        val hotCount = counts["hot"] ?: 0
        val coldCount = counts["cold"] ?: 0
        assertTrue("expected hot >> cold, got hot=$hotCount cold=$coldCount", hotCount > coldCount * 5)
    }

    @Test fun samplingWithoutReplacement() {
        val a = medium("a", "music")
        val b = medium("b", "music")
        val picks = SessionBuilder.sampleByInterest(
            media = listOf(a, b),
            weights = mapOf("music" to 1.0),
            take = 5,          // asked for 5, only 2 available
            rng = Random(0)
        )
        assertEquals(2, picks.size)
        assertEquals(setOf("a", "b"), picks.map { it.id }.toSet())
    }
}
