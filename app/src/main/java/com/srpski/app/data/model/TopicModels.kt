package com.srpski.app.data.model

import kotlinx.serialization.Serializable

/** Fixed 15-topic taxonomy for interest onboarding + implicit tagging. */
@Serializable
data class TopicDef(
    val id: String,
    val label: String,
    /** Keywords used by the article classifier (case-insensitive substring). */
    val keywords: List<String> = emptyList(),
    /** YouTube Data API v3 category IDs that map to this topic. */
    val youtubeCategories: List<Int> = emptyList()
)

@Serializable
data class TopicMap(
    val schemaVersion: Int,
    val topics: List<TopicDef>
)
