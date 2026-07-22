package com.srpski.app.data.media

import com.srpski.app.data.db.SharedMedium

/**
 * Pure classifier for shared URLs. No network, no Android APIs — testable on JVM.
 *
 * Returns [SharedMedium.KIND_YOUTUBE] with the videoId parsed out of the URL,
 * [SharedMedium.KIND_ARTICLE] for anything that parses as an http(s) URL,
 * or [SharedMedium.KIND_TEXT] as a fallback for plain text shares.
 */
object UrlClassifier {

    data class Classified(val kind: String, val youtubeId: String? = null, val url: String? = null)

    private val YOUTUBE_HOSTS = setOf(
        "www.youtube.com", "youtube.com", "m.youtube.com", "music.youtube.com",
        "youtu.be", "www.youtu.be"
    )

    private val URL_REGEX = Regex("""https?://\S+""")

    fun classify(raw: String): Classified {
        val trimmed = raw.trim()
        val urlToken = firstUrl(trimmed) ?: return Classified(SharedMedium.KIND_TEXT)
        val ytId = extractYouTubeId(urlToken)
        return when {
            ytId != null -> Classified(SharedMedium.KIND_YOUTUBE, youtubeId = ytId, url = urlToken)
            else -> Classified(SharedMedium.KIND_ARTICLE, url = urlToken)
        }
    }

    fun firstUrl(text: String): String? = URL_REGEX.find(text)?.value

    /** Returns the 11-char YouTube video id or null. Handles youtu.be, watch?v=, /shorts/, /embed/. */
    fun extractYouTubeId(url: String): String? {
        val parsed = runCatching { java.net.URI(url) }.getOrNull() ?: return null
        val host = parsed.host?.lowercase() ?: return null
        if (host !in YOUTUBE_HOSTS) return null

        if (host == "youtu.be" || host == "www.youtu.be") {
            return parsed.path?.removePrefix("/")?.takeIf { isValidVideoId(it) }
        }

        val path = parsed.path ?: "/"
        // /watch?v=ID
        parsed.query?.split('&')?.forEach { part ->
            val (k, v) = part.split('=', limit = 2).let {
                if (it.size == 2) it[0] to it[1] else it[0] to ""
            }
            if (k == "v" && isValidVideoId(v)) return v
        }
        // /shorts/ID, /embed/ID, /live/ID
        val segments = path.trim('/').split('/')
        if (segments.size >= 2 && segments[0] in setOf("shorts", "embed", "live", "v")) {
            val id = segments[1]
            if (isValidVideoId(id)) return id
        }
        return null
    }

    private val YT_ID_REGEX = Regex("^[A-Za-z0-9_-]{11}$")
    private fun isValidVideoId(id: String): Boolean = YT_ID_REGEX.matches(id)
}
