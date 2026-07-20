package com.srpski.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {
    @Query("SELECT * FROM lesson_progress")
    fun lessonProgress(): Flow<List<LessonProgress>>

    @Query("SELECT * FROM lesson_progress WHERE lessonId = :id")
    suspend fun lessonProgressFor(id: String): LessonProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLessonProgress(p: LessonProgress)

    @Query("SELECT * FROM srs_cards WHERE dueEpochDay <= :today ORDER BY dueEpochDay LIMIT :limit")
    suspend fun dueCards(today: Long, limit: Int): List<SrsCard>

    @Query("SELECT * FROM srs_cards WHERE vocabId = :vocabId")
    suspend fun card(vocabId: String): SrsCard?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCard(card: SrsCard)

    @Query("SELECT * FROM srs_cards ORDER BY (wrong * 1.0) / (correct + wrong + 1) DESC LIMIT :limit")
    suspend fun weakest(limit: Int): List<SrsCard>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDaily(stat: DailyStat)

    @Query("SELECT * FROM daily_stats WHERE epochDay = :day")
    suspend fun daily(day: Long): DailyStat?

    @Query("SELECT * FROM daily_stats ORDER BY epochDay DESC LIMIT 60")
    suspend fun recentDays(): List<DailyStat>
}
