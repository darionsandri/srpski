package com.srpski.app.data.model

import kotlinx.serialization.Serializable

/** On-disk payload for a YouTube-shared item. Stored at filesDir/media/<sharedMediaId>.json. */
@Serializable
data class YouTubeMeta(
    val videoId: String,
    val title: String,
    val description: String = "",
    val channelTitle: String = "",
    val categoryId: Int? = null,
    val tags: List<String> = emptyList(),
    val durationSec: Int? = null
)

/** On-disk payload for an article-shared item. Stored at filesDir/media/<sharedMediaId>.json. */
@Serializable
data class ArticleDoc(
    val sourceUrl: String,
    val title: String,
    val byline: String? = null,
    val language: String = "sr",
    val textSr: String = "",
    val textEn: String? = null,
    val excerpt: String = ""
)
