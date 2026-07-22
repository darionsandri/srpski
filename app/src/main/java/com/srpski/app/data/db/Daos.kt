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

@Dao
interface ContentPackDao {
    @Query("SELECT * FROM content_packs ORDER BY installedAtEpochSec")
    fun all(): Flow<List<ContentPack>>

    @Query("SELECT * FROM content_packs WHERE enabled = 1 ORDER BY installedAtEpochSec")
    suspend fun enabled(): List<ContentPack>

    @Query("SELECT * FROM content_packs WHERE id = :id")
    suspend fun byId(id: String): ContentPack?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(pack: ContentPack)

    @Query("DELETE FROM content_packs WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface InterestDao {
    @Query("SELECT * FROM interests")
    fun all(): Flow<List<InterestWeight>>

    @Query("SELECT * FROM interests")
    suspend fun snapshot(): List<InterestWeight>

    @Query("SELECT * FROM interests WHERE topicId = :topicId")
    suspend fun byTopic(topicId: String): InterestWeight?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(weight: InterestWeight)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(weights: List<InterestWeight>)

    @Query("SELECT COUNT(*) FROM interests")
    suspend fun count(): Int
}

@Dao
interface SharedMediaDao {
    @Query("SELECT * FROM shared_media ORDER BY sharedAtEpochSec DESC")
    fun all(): Flow<List<SharedMedium>>

    @Query("SELECT * FROM shared_media WHERE status = :status ORDER BY sharedAtEpochSec")
    suspend fun byStatus(status: String): List<SharedMedium>

    @Query("SELECT * FROM shared_media WHERE id = :id")
    suspend fun byId(id: String): SharedMedium?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(medium: SharedMedium)

    @Query("DELETE FROM shared_media WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface MediaExerciseDao {
    @Query("SELECT * FROM media_exercises WHERE mediaId = :mediaId ORDER BY generatedAtEpochSec DESC")
    suspend fun forMedia(mediaId: String): List<MediaExercise>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(ex: MediaExercise)

    @Query("DELETE FROM media_exercises WHERE mediaId = :mediaId")
    suspend fun deleteForMedia(mediaId: String)
}
