package com.srpski.app.data.media

import com.srpski.app.data.model.YouTubeMeta
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

/**
 * Thin wrapper over YouTube Data API v3 `videos.list` for fetching metadata
 * from a shared video id. Only used to derive title/tags/category for the
 * topic classifier — no video streaming (that goes through the IFrame player).
 *
 * The API key is provided at construction time (from BuildConfig) so tests
 * can inject a stub client. Returns null on any HTTP or parse failure —
 * callers should treat it as best-effort enrichment.
 */
class YouTubeApi(
    private val client: OkHttpClient,
    private val apiKey: String,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    fun fetchMeta(videoId: String): YouTubeMeta? {
        if (apiKey.isBlank()) return null
        val url = "https://www.googleapis.com/youtube/v3/videos".toHttpUrl()
            .newBuilder()
            .addQueryParameter("id", videoId)
            .addQueryParameter("part", "snippet,contentDetails")
            .addQueryParameter("key", apiKey)
            .build()

        val request = Request.Builder().url(url).get().build()
        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body?.string() ?: return null
                parseMeta(videoId, body)
            }
        } catch (_: IOException) {
            null
        }
    }

    internal fun parseMeta(videoId: String, body: String): YouTubeMeta? {
        val root = runCatching { json.parseToJsonElement(body).jsonObject }.getOrNull() ?: return null
        val items = root["items"]?.jsonArray ?: return null
        if (items.isEmpty()) return null
        val first = items[0].jsonObject
        val snippet = first["snippet"]?.jsonObject ?: return null
        val title = snippet["title"]?.jsonPrimitive?.content ?: ""
        val description = snippet["description"]?.jsonPrimitive?.content.orEmpty()
        val channelTitle = snippet["channelTitle"]?.jsonPrimitive?.content.orEmpty()
        val categoryId = snippet["categoryId"]?.jsonPrimitive?.content?.toIntOrNull()
        val tags = snippet["tags"]?.jsonArray?.mapNotNull { it.jsonPrimitive.content } ?: emptyList()
        val duration = first["contentDetails"]?.jsonObject
            ?.get("duration")?.jsonPrimitive?.content
            ?.let { parseIso8601Seconds(it) }
        return YouTubeMeta(
            videoId = videoId,
            title = title,
            description = description,
            channelTitle = channelTitle,
            categoryId = categoryId,
            tags = tags,
            durationSec = duration
        )
    }

    companion object {
        /** Parses `PT#H#M#S` into total seconds. Returns null on malformed. */
        fun parseIso8601Seconds(iso: String): Int? {
            val m = Regex("""^PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?$""").matchEntire(iso) ?: return null
            val (h, min, s) = m.destructured
            val total = (h.toIntOrNull() ?: 0) * 3600 +
                (min.toIntOrNull() ?: 0) * 60 +
                (s.toIntOrNull() ?: 0)
            return total
        }
    }
}
