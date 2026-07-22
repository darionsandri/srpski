package com.srpski.app.domain

import com.srpski.app.data.db.SharedMedium
import com.srpski.app.data.model.ExerciseDef
import com.srpski.app.data.model.VocabItem
import kotlin.random.Random

/**
 * Builds a lesson session: the lesson's own exercises in authored order
 * (authored order ramps difficulty deliberately), plus up to [maxReviews]
 * SRS-due review exercises appended at the end.
 *
 * Review exercises are generated as multiple choice (en -> sr) using
 * random distractors from the vocab pool.
 */
object SessionBuilder {

    const val MAX_REVIEWS = 5

    fun build(
        lessonExercises: List<ExerciseDef>,
        dueVocab: List<VocabItem>,
        vocabPool: List<VocabItem>,
        maxReviews: Int = MAX_REVIEWS
    ): List<ExerciseDef> {
        val reviews = dueVocab
            .filter { it.en.isNotEmpty() }
            .take(maxReviews)
            .mapNotNull { target -> makeReview(target, vocabPool) }
        return lessonExercises + reviews
    }

    /**
     * Weak-words session: multiple-choice review of the N vocab items with the
     * worst accuracy ratio in the SRS. Uses the same [makeReview] machinery
     * as the review appendix on regular lessons.
     */
    fun buildWeakWords(
        weakVocab: List<VocabItem>,
        vocabPool: List<VocabItem>,
        maxItems: Int = 10
    ): List<ExerciseDef> =
        weakVocab
            .filter { it.en.isNotEmpty() }
            .take(maxItems)
            .mapNotNull { makeReview(it, vocabPool) }

    /**
     * Weighted sampling of shared media by interest overlap.
     * Score(medium) = sum(interestWeights[t] for t in medium.tagsCsv).
     * Items with zero score are excluded. Sampling is without replacement.
     */
    fun sampleByInterest(
        media: List<SharedMedium>,
        weights: Map<String, Double>,
        take: Int = 3,
        rng: Random = Random.Default
    ): List<SharedMedium> {
        if (media.isEmpty() || take <= 0) return emptyList()
        val scored = media.mapNotNull { m ->
            val tags = m.tagsCsv.split(',').map { it.trim() }.filter { it.isNotEmpty() }
            if (tags.isEmpty()) return@mapNotNull null
            val score = tags.sumOf { weights[it] ?: 0.0 }
            if (score <= 0.0) null else m to score
        }.toMutableList()
        if (scored.isEmpty()) return emptyList()

        val picked = mutableListOf<SharedMedium>()
        repeat(take.coerceAtMost(scored.size)) {
            val total = scored.sumOf { it.second }
            if (total <= 0.0) return@repeat
            var draw = rng.nextDouble(total)
            val index = scored.indexOfFirst { entry ->
                draw -= entry.second
                draw <= 0.0
            }.coerceAtLeast(0)
            picked += scored[index].first
            scored.removeAt(index)
        }
        return picked
    }

    private fun makeReview(target: VocabItem, pool: List<VocabItem>): ExerciseDef? {
        val distractors = pool
            .filter { it.id != target.id }
            .shuffled()
            .take(3)
            .map { it.sr }
        if (distractors.size < 3) return null
        val options = (distractors + target.sr).shuffled()
        return ExerciseDef(
            id = "review_${target.id}",
            type = "multipleChoice",
            promptEn = target.en.first(),
            options = options,
            correctIndex = options.indexOf(target.sr),
            vocabRefs = listOf(target.id)
        )
    }
}
