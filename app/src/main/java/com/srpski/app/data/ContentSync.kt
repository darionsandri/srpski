package com.srpski.app.data

import com.srpski.app.data.db.ContentPack
import com.srpski.app.data.db.ContentPackDao
import com.srpski.app.data.model.PackIndex
import com.srpski.app.data.model.PackManifest
import com.srpski.app.data.model.PackSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.security.MessageDigest

/**
 * Syncs the remote pack index (GitHub Pages static JSON) with the local
 * `content_packs` table, and installs/uninstalls per-pack payloads on disk.
 */
class ContentSync(
    private val http: OkHttpClient,
    private val dao: ContentPackDao,
    private val content: ContentRepository,
    private val packsDir: File,
    private val indexUrl: String,
    private val clock: () -> Long = { System.currentTimeMillis() / 1000L }
) {
    private val json = Json { ignoreUnknownKeys = true }

    data class AvailablePack(val summary: PackSummary, val installedVersion: Int?)

    fun installedFlow(): Flow<List<ContentPack>> = dao.all()

    /** Returns the remote index joined with what is already installed. Best-effort. */
    suspend fun refreshIndex(): List<AvailablePack> = withContext(Dispatchers.IO) {
        val body = get(indexUrl) ?: return@withContext emptyList()
        val index = runCatching { json.decodeFromString<PackIndex>(body) }.getOrNull()
            ?: return@withContext emptyList()
        val installed = dao.enabled().associateBy { it.id }
        index.packs.map { p -> AvailablePack(p, installed[p.id]?.version) }
    }

    suspend fun install(pack: PackSummary): Result<ContentPack> = withContext(Dispatchers.IO) {
        runCatching {
            packsDir.mkdirs()
            val target = File(packsDir, pack.id).apply { deleteRecursively(); mkdirs() }
            val manifestBytes = getBytes(pack.url) ?: error("Failed to download manifest")
            val hash = sha256Hex(manifestBytes)
            if (pack.sha256.isNotBlank() && hash != pack.sha256) {
                error("Checksum mismatch (expected ${pack.sha256}, got $hash)")
            }
            File(target, "manifest.json").writeBytes(manifestBytes)

            val manifest = json.decodeFromString<PackManifest>(manifestBytes.decodeToString())
            manifest.unitFiles.forEach { relative ->
                val fileUrl = joinUrl(pack.url, relative)
                val fileBytes = getBytes(fileUrl) ?: error("Failed to download $relative")
                File(target, relative).writeBytes(fileBytes)
            }

            val row = ContentPack(
                id = pack.id,
                version = pack.version,
                sha256 = pack.sha256,
                sourceUrl = pack.url,
                installedAtEpochSec = clock(),
                enabled = true
            )
            dao.upsert(row)
            content.refresh()
            row
        }
    }

    suspend fun uninstall(packId: String) = withContext(Dispatchers.IO) {
        dao.delete(packId)
        File(packsDir, packId).deleteRecursively()
        content.refresh()
    }

    // --- helpers ---

    private fun get(url: String): String? {
        val request = Request.Builder().url(url).get().build()
        return runCatching {
            http.newCall(request).execute().use { r ->
                if (!r.isSuccessful) null else r.body?.string()
            }
        }.getOrNull()
    }

    private fun getBytes(url: String): ByteArray? {
        val request = Request.Builder().url(url).get().build()
        return runCatching {
            http.newCall(request).execute().use { r ->
                if (!r.isSuccessful) null else r.body?.bytes()
            }
        }.getOrNull()
    }

    companion object {
        internal fun joinUrl(base: String, relative: String): String {
            val idx = base.lastIndexOf('/')
            val prefix = if (idx >= 0) base.substring(0, idx + 1) else "$base/"
            return prefix + relative.removePrefix("/")
        }

        internal fun sha256Hex(bytes: ByteArray): String {
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            return digest.joinToString("") { b -> "%02x".format(b) }
        }
    }
}
