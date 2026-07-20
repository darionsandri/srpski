package com.srpski.app.domain

import com.srpski.app.data.db.SrsCard

/**
 * Simplified Leitner-style spaced repetition.
 * Boxes 0..5 with review intervals in days. Correct answer -> next box.
 * Wrong answer -> back to box 0, due tomorrow.
 */
object Srs {
    private val intervals = intArrayOf(1, 2, 4, 7, 15, 30)

    fun onAnswer(card: SrsCard, correct: Boolean, todayEpochDay: Long): SrsCard {
        return if (correct) {
            val newBox = (card.box + 1).coerceAtMost(intervals.size - 1)
            card.copy(
                box = newBox,
                dueEpochDay = todayEpochDay + intervals[newBox],
                correct = card.correct + 1
            )
        } else {
            card.copy(
                box = 0,
                dueEpochDay = todayEpochDay + 1,
                wrong = card.wrong + 1
            )
        }
    }

    fun newCard(vocabId: String, todayEpochDay: Long): SrsCard =
        SrsCard(vocabId = vocabId, box = 0, dueEpochDay = todayEpochDay + intervals[0])
}
