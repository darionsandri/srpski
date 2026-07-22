package com.srpski.app.data

import com.srpski.app.data.db.DailyStat
import com.srpski.app.data.db.LessonProgress
import com.srpski.app.data.db.ProgressDao
import com.srpski.app.data.db.SrsCard
import com.srpski.app.domain.Srs
import com.srpski.app.domain.Streaks
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class ProgressRepository(private val dao: ProgressDao) {

    fun today(): Long = LocalDate.now().toEpochDay()

    suspend fun dueVocabIds(limit: Int): List<String> =
        dao.dueCards(today(), limit).map { it.vocabId }

    suspend fun weakestVocabIds(limit: Int): List<String> =
        dao.weakest(limit).map { it.vocabId }

    suspend fun recordVocabAnswer(vocabId: String, correct: Boolean) {
        val card = dao.card(vocabId) ?: Srs.newCard(vocabId, today())
        dao.upsertCard(Srs.onAnswer(card, correct, today()))
    }

    suspend fun addXp(amount: Int) {
        val day = today()
        val stat = dao.daily(day) ?: DailyStat(day, 0)
        dao.upsertDaily(stat.copy(xp = stat.xp + amount))
    }

    suspend fun recordLessonComplete(lessonId: String) {
        val existing = dao.lessonProgressFor(lessonId)
        dao.upsertLessonProgress(
            LessonProgress(
                lessonId = lessonId,
                completedCount = (existing?.completedCount ?: 0) + 1,
                lastCompletedEpochDay = today()
            )
        )
    }

    suspend fun todayXp(): Int = dao.daily(today())?.xp ?: 0

    suspend fun currentStreak(): Int = Streaks.currentStreak(dao.recentDays(), today())

    suspend fun totalXp(): Int = dao.recentDays().sumOf { it.xp }

    suspend fun completedLessonCount(): Int =
        dao.lessonProgress().first().count { it.completedCount > 0 }

    suspend fun totalCorrectAnswers(): Int =
        dao.weakest(Int.MAX_VALUE).sumOf { it.correct }

    fun lessonProgressFlow() = dao.lessonProgress()
}
