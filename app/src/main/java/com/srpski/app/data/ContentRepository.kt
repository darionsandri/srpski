package com.srpski.app.data

import android.content.Context
import com.srpski.app.data.model.CourseFile
import com.srpski.app.data.model.ExerciseDef
import com.srpski.app.data.model.LessonDef
import com.srpski.app.data.model.UnitContentFile
import com.srpski.app.data.model.VocabItem
import kotlinx.serialization.json.Json

/**
 * Loads bundled JSON content from assets. Content is immutable at runtime;
 * user progress lives in Room. Kept in memory after first load (content is small).
 */
class ContentRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    val course: CourseFile by lazy {
        json.decodeFromString(readAsset("content/course.json"))
    }

    private val unitFiles: Map<String, UnitContentFile> by lazy {
        course.units.associate { unit ->
            val fileName = "content/unit${unit.id.removePrefix("u")}.json"
            unit.id to json.decodeFromString<UnitContentFile>(readAsset(fileName))
        }
    }

    val vocabById: Map<String, VocabItem> by lazy {
        unitFiles.values.flatMap { it.vocab }.associateBy { it.id }
    }

    fun lesson(lessonId: String): LessonDef? =
        unitFiles.values.asSequence()
            .flatMap { it.lessons.asSequence() }
            .firstOrNull { it.id == lessonId }

    fun lessonsForUnit(unitId: String): List<LessonDef> =
        unitFiles[unitId]?.lessons ?: emptyList()

    fun exercisesReferencing(vocabId: String): List<ExerciseDef> =
        unitFiles.values.flatMap { u -> u.lessons.flatMap { it.exercises } }
            .filter { vocabId in it.vocabRefs }

    private fun readAsset(path: String): String =
        context.assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }
}
