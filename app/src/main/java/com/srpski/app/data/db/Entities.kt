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

// --- M6 additions ---

/** Registry row for an installed content pack. Payload JSON lives on disk. */
@Entity(tableName = "content_packs")
data class ContentPack(
    @PrimaryKey val id: String,
    val version: Int,
    val sha256: String,
    val sourceUrl: String,
    val installedAtEpochSec: Long,
    val enabled: Boolean = true
)

/** Topic interest weight in [0.05, 1.0]. */
@Entity(tableName = "interests")
data class InterestWeight(
    @PrimaryKey val topicId: String,
    val weight: Double,
    val updatedAtEpochSec: Long
)

/** Media a user shared INTO the app. Payload lives at filesDir/media/<id>.json. */
@Entity(tableName = "shared_media")
data class SharedMedium(
    @PrimaryKey val id: String,           // UUID
    val url: String,
    val kind: String,                     // youtube | article | text
    val title: String? = null,
    val tagsCsv: String = "",             // comma-separated topic ids
    val sharedAtEpochSec: Long,
    val status: String = STATUS_RAW       // raw | enriched | exercised | failed
) {
    companion object {
        const val STATUS_RAW = "raw"
        const val STATUS_ENRICHED = "enriched"
        const val STATUS_EXERCISED = "exercised"
        const val STATUS_FAILED = "failed"

        const val KIND_YOUTUBE = "youtube"
        const val KIND_ARTICLE = "article"
        const val KIND_TEXT = "text"
    }
}

/** Generated exercise for a shared medium. Cached to avoid re-generation. */
@Entity(tableName = "media_exercises")
data class MediaExercise(
    @PrimaryKey val id: String,           // UUID
    val mediaId: String,
    val exerciseJson: String,             // serialized ExerciseDef
    val generatedAtEpochSec: Long
)
