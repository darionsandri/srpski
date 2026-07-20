package com.srpski.app.domain

import com.srpski.app.data.model.ExerciseDef
import com.srpski.app.data.model.VocabItem

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
