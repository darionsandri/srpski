package com.srpski.app.data

import com.srpski.app.data.db.SharedMedium
import com.srpski.app.data.db.SharedMediaDao
import com.srpski.app.data.media.ArticleFetcher
import com.srpski.app.data.media.TopicClassifier
import com.srpski.app.data.media.UrlClassifier
import com.srpski.app.data.media.YouTubeApi
import com.srpski.app.data.model.ArticleDoc
import com.srpski.app.data.model.YouTubeMeta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

/**
 * Orchestrates share ingest end-to-end:
 *   raw text → UrlClassifier → (YouTube metadata OR raw text) → topic tags
 *   → InterestRepository.reinforce → SharedMedium row + on-disk payload.
 *
 * Returns the persisted [SharedMedium] so the UI can navigate to a preview.
 */
class SharedMediaRepository(
    private val dao: SharedMediaDao,
    private val youTubeApi: YouTubeApi?,
    private val articleFetcher: ArticleFetcher?,
    private val topicClassifier: TopicClassifier,
    private val interests: InterestRepository,
    private val mediaDir: File,
    private val clock: () -> Long = { System.currentTimeMillis() / 1000L },
    private val idGen: () -> String = { UUID.randomUUID().toString() }
) {

    private val json = Json { prettyPrint = false; ignoreUnknownKeys = true }

    data class IngestResult(val medium: SharedMedium, val detectedTopics: List<String>)

    suspend fun ingest(rawText: String): IngestResult = withContext(Dispatchers.IO) {
        mediaDir.mkdirs()
        val id = idGen()
        val classified = UrlClassifier.classify(rawText)
        val nowSec = clock()

        val (medium, topics) = when (classified.kind) {
            SharedMedium.KIND_YOUTUBE -> ingestYouTube(id, classified.url ?: rawText, classified.youtubeId!!, nowSec)
            SharedMedium.KIND_ARTICLE -> ingestArticle(id, classified.url ?: rawText, nowSec)
            else -> ingestText(id, rawText, nowSec)
        }

        interests.reinforce(topics)
        dao.upsert(medium)
        IngestResult(medium, topics)
    }

    private fun ingestYouTube(id: String, url: String, videoId: String, nowSec: Long): Pair<SharedMedium, List<String>> {
        val meta = youTubeApi?.fetchMeta(videoId)
        val payloadFile = File(mediaDir, "$id.json")
        if (meta != null) {
            payloadFile.writeText(json.encodeToString(meta))
        } else {
            payloadFile.writeText(json.encodeToString(YouTubeMeta(videoId = videoId, title = "")))
        }
        val topics = meta?.let { topicClassifier.classifyYouTube(it) }.orEmpty()
        val medium = SharedMedium(
            id = id,
            url = url,
            kind = SharedMedium.KIND_YOUTUBE,
            title = meta?.title,
            tagsCsv = topics.joinToString(","),
            sharedAtEpochSec = nowSec,
            status = if (meta != null) SharedMedium.STATUS_ENRICHED else SharedMedium.STATUS_RAW
        )
        return medium to topics
    }

    private fun ingestArticle(id: String, url: String, nowSec: Long): Pair<SharedMedium, List<String>> {
        val doc: ArticleDoc? = articleFetcher?.fetch(url)
        val payloadFile = File(mediaDir, "$id.json")
        if (doc != null) {
            payloadFile.writeText(json.encodeToString(doc))
        }
        val topics = doc?.let { topicClassifier.classifyArticle(it) }.orEmpty()
        val medium = SharedMedium(
            id = id,
            url = url,
            kind = SharedMedium.KIND_ARTICLE,
            title = doc?.title,
            tagsCsv = topics.joinToString(","),
            sharedAtEpochSec = nowSec,
            status = if (doc != null) SharedMedium.STATUS_ENRICHED else SharedMedium.STATUS_RAW
        )
        return medium to topics
    }

    private fun ingestText(id: String, rawText: String, nowSec: Long): Pair<SharedMedium, List<String>> {
        val medium = SharedMedium(
            id = id,
            url = "",
            kind = SharedMedium.KIND_TEXT,
            title = rawText.take(80),
            tagsCsv = "",
            sharedAtEpochSec = nowSec,
            status = SharedMedium.STATUS_RAW
        )
        return medium to emptyList()
    }

    fun observeAll() = dao.all()

    suspend fun count(): Int = dao.all().first().size
}
