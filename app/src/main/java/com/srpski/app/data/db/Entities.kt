package com.srpski.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lesson_progress")
data class LessonProgress(
    @PrimaryKey val lessonId: String,
    val completedCount: Int = 0,
    val lastCompletedEpochDay: Long = 0
)

@Entity(tableName = "srs_cards")
data class SrsCard(
    @PrimaryKey val vocabId: String,
    val box: Int = 0,            // 0..5 Leitner box
    val dueEpochDay: Long = 0,   // next review day
    val correct: Int = 0,
    val wrong: Int = 0
)

@Entity(tableName = "daily_stats")
data class DailyStat(
    @PrimaryKey val epochDay: Long,
    val xp: Int = 0
)
