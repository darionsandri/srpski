package com.srpski.app.data.media

import com.srpski.app.data.model.ArticleDoc
import com.srpski.app.data.model.TopicDef
import com.srpski.app.data.model.TopicMap
import com.srpski.app.data.model.YouTubeMeta

/**
 * Pure classifier — maps a shared medium to a set of topic ids from [TopicMap].
 * Uses YouTube category + tag hits, or keyword bag for articles. No ML.
 */
class TopicClassifier(private val topicMap: TopicMap) {

    /** Returns topic ids sorted by descending score, or empty if nothing matched. */
    fun classifyYouTube(meta: YouTubeMeta): List<String> {
        val scores = HashMap<String, Double>()
        for (topic in topicMap.topics) {
            var score = 0.0
            if (meta.categoryId != null && meta.categoryId in topic.youtubeCategories) score += 2.0
            score += keywordHits(topic, meta.title + " " + meta.description) * 1.0
            score += tagHits(topic, meta.tags) * 1.5
            if (score > 0.0) scores[topic.id] = score
        }
        return scores.entries.sortedByDescending { it.value }.map { it.key }
    }

    fun classifyArticle(doc: ArticleDoc): List<String> {
        val corpus = buildString {
            append(doc.title).append(' ')
            append(doc.excerpt).append(' ')
            append(doc.textSr.take(2000))
        }
        val scores = HashMap<String, Double>()
        for (topic in topicMap.topics) {
            val hits = keywordHits(topic, corpus).toDouble()
            if (hits > 0.0) scores[topic.id] = hits
        }
        return scores.entries.sortedByDescending { it.value }.map { it.key }
    }

    private fun keywordHits(topic: TopicDef, text: String): Int {
        if (text.isBlank()) return 0
        val lower = text.lowercase()
        var count = 0
        for (kw in topic.keywords) {
            if (kw.isBlank()) continue
            if (lower.contains(kw.lowercase())) count++
        }
        return count
    }

    private fun tagHits(topic: TopicDef, tags: List<String>): Int {
        if (tags.isEmpty()) return 0
        var count = 0
        for (tag in tags) {
            val lower = tag.lowercase()
            for (kw in topic.keywords) {
                if (kw.isNotBlank() && lower.contains(kw.lowercase())) {
                    count++; break
                }
            }
        }
        return count
    }
}
