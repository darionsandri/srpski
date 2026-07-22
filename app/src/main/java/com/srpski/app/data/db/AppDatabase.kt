package com.srpski.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        LessonProgress::class,
        SrsCard::class,
        DailyStat::class,
        ContentPack::class,
        InterestWeight::class,
        SharedMedium::class,
        MediaExercise::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun progressDao(): ProgressDao
    abstract fun contentPackDao(): ContentPackDao
    abstract fun interestDao(): InterestDao
    abstract fun sharedMediaDao(): SharedMediaDao
    abstract fun mediaExerciseDao(): MediaExerciseDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "srpski.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `content_packs` (
                        `id` TEXT NOT NULL,
                        `version` INTEGER NOT NULL,
                        `sha256` TEXT NOT NULL,
                        `sourceUrl` TEXT NOT NULL,
                        `installedAtEpochSec` INTEGER NOT NULL,
                        `enabled` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `interests` (
                        `topicId` TEXT NOT NULL,
                        `weight` REAL NOT NULL,
                        `updatedAtEpochSec` INTEGER NOT NULL,
                        PRIMARY KEY(`topicId`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `shared_media` (
                        `id` TEXT NOT NULL,
                        `url` TEXT NOT NULL,
                        `kind` TEXT NOT NULL,
                        `title` TEXT,
                        `tagsCsv` TEXT NOT NULL,
                        `sharedAtEpochSec` INTEGER NOT NULL,
                        `status` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `media_exercises` (
                        `id` TEXT NOT NULL,
                        `mediaId` TEXT NOT NULL,
                        `exerciseJson` TEXT NOT NULL,
                        `generatedAtEpochSec` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
