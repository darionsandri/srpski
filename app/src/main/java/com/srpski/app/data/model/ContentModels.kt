package com.srpski.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CourseFile(
    val schemaVersion: Int,
    val course: String,
    val units: List<UnitDef>
)

@Serializable
data class UnitDef(
    val id: String,
    val title: String,
    val cefr: String,
    val description: String = "",
    val requiresUnit: String? = null,
    val lessons: List<String>
)

@Serializable
data class UnitContentFile(
    val unitId: String,
    val vocab: List<VocabItem>,
    val lessons: List<LessonDef>
)

@Serializable
data class VocabItem(
    val id: String,
    val sr: String,
    val en: List<String>,
    val pos: String? = null,
    val notes: String? = null,
    val tags: List<String> = emptyList()
)

@Serializable
data class LessonDef(
    val id: String,
    val title: String,
    val exercises: List<ExerciseDef>
)

/**
 * Flat exercise model. Which fields are used depends on [type]:
 * multipleChoice: promptSr|promptEn, options, correctIndex
 * wordBank:       promptEn, tiles, accepted
 * typing:         promptEn, accepted
 * listening:      audioText, accepted
 * fillBlank:      sentence, options, correctIndex
 * videoTranslate: (Phase M6) youtubeId, startSec, endSec, transcriptSr, accepted
 */
@Serializable
data class ExerciseDef(
    val id: String,
    val type: String,
    val promptEn: String? = null,
    val promptSr: String? = null,
    val sentence: String? = null,
    val audioText: String? = null,
    val options: List<String> = emptyList(),
    val correctIndex: Int? = null,
    val tiles: List<String> = emptyList(),
    val accepted: List<String> = emptyList(),
    val diacriticsSensitive: Boolean = true,
    val vocabRefs: List<String> = emptyList(),
    val grammarRefs: List<String> = emptyList(),
    val youtubeId: String? = null,
    val startSec: Int? = null,
    val endSec: Int? = null,
    val transcriptSr: String? = null
)
