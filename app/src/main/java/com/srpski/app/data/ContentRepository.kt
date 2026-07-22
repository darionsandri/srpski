package com.srpski.app.data

import android.content.Context
import com.srpski.app.data.model.CourseFile
import com.srpski.app.data.model.ExerciseDef
import com.srpski.app.data.model.GrammarIndex
import com.srpski.app.data.model.GrammarNote
import com.srpski.app.data.model.LessonDef
import com.srpski.app.data.model.PackManifest
import com.srpski.app.data.model.UnitContentFile
import com.srpski.app.data.model.UnitDef
import com.srpski.app.data.model.VocabItem
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Loads bundled JSON content from assets AND merges downloaded packs from
 * `filesDir/packs/<packId>/`. Downloaded packs can add units or override
 * a same-id bundled unit (later version wins). Kept in memory after first
 * load (content is small); callers invoke [refresh] after installing a pack.
 */
class ContentRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private val packsDir: File get() = File(context.filesDir, "packs")

    @Volatile private var snapshot: Snapshot = load()

    val course: CourseFile get() = snapshot.course
    val vocabById: Map<String, VocabItem> get() = snapshot.vocabById

    /** Idempotent — safe to call after pack install/uninstall. */
    fun refresh() {
        snapshot = load()
    }

    fun lesson(lessonId: String): LessonDef? =
        snapshot.unitFiles.values.asSequence()
            .flatMap { it.lessons.asSequence() }
            .firstOrNull { it.id == lessonId }

    fun lessonsForUnit(unitId: String): List<LessonDef> =
        snapshot.unitFiles[unitId]?.lessons ?: emptyList()

    fun exercisesReferencing(vocabId: String): List<ExerciseDef> =
        snapshot.unitFiles.values.flatMap { u -> u.lessons.flatMap { it.exercises } }
            .filter { vocabId in it.vocabRefs }

    val grammarNotes: Map<String, GrammarNote> by lazy {
        val index = runCatching {
            json.decodeFromString<GrammarIndex>(readAsset("grammar/index.json"))
        }.getOrNull() ?: return@lazy emptyMap()
        index.notes.mapNotNull { fileName ->
            runCatching {
                json.decodeFromString<GrammarNote>(readAsset("grammar/$fileName"))
            }.getOrNull()
        }.associateBy { it.id }
    }

    fun grammarNote(id: String): GrammarNote? = grammarNotes[id]

    // --- internals ---

    private data class Snapshot(
        val course: CourseFile,
        val unitFiles: Map<String, UnitContentFile>,
        val vocabById: Map<String, VocabItem>
    )

    private fun load(): Snapshot {
        val bundledCourse: CourseFile = json.decodeFromString(readAsset("content/course.json"))
        val bundledUnits: Map<String, UnitContentFile> = bundledCourse.units.associate { unit ->
            val fileName = "content/unit${unit.id.removePrefix("u")}.json"
            unit.id to json.decodeFromString<UnitContentFile>(readAsset(fileName))
        }

        val packUnitsByPack: List<Pair<UnitDef, UnitContentFile>> = loadInstalledPacks()

        // Merge units: bundled base, overridden by packs of matching id, with new units appended.
        val mergedUnits = LinkedHashMap<String, UnitDef>()
        val mergedFiles = LinkedHashMap<String, UnitContentFile>()
        bundledCourse.units.forEach { mergedUnits[it.id] = it }
        bundledUnits.forEach { (id, file) -> mergedFiles[id] = file }
        packUnitsByPack.forEach { (unit, file) ->
            mergedUnits[unit.id] = unit
            mergedFiles[unit.id] = file
        }

        val course = bundledCourse.copy(units = mergedUnits.values.toList())
        val vocab = mergedFiles.values.flatMap { it.vocab }.associateBy { it.id }
        return Snapshot(course, mergedFiles, vocab)
    }

    private fun loadInstalledPacks(): List<Pair<UnitDef, UnitContentFile>> {
        val dir = packsDir
        if (!dir.exists()) return emptyList()
        val out = mutableListOf<Pair<UnitDef, UnitContentFile>>()
        dir.listFiles { f -> f.isDirectory }?.sortedBy { it.name }?.forEach { packDir ->
            runCatching {
                val manifestFile = File(packDir, "manifest.json")
                if (!manifestFile.exists()) return@runCatching
                val manifest = json.decodeFromString<PackManifest>(manifestFile.readText())
                manifest.units.zip(manifest.unitFiles).forEach { (unit, unitFileName) ->
                    val unitFile = File(packDir, unitFileName)
                    if (!unitFile.exists()) return@forEach
                    val content = json.decodeFromString<UnitContentFile>(unitFile.readText())
                    out += unit to content
                }
            }
        }
        return out
    }

    private fun readAsset(path: String): String =
        context.assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }
}
