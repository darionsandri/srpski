package com.srpski.app.data.model

import kotlinx.serialization.Serializable

/** Short grammar concept shown to the user before a lesson references it. */
@Serializable
data class GrammarNote(
    val id: String,
    val title: String,
    /** Plain markdown-ish body — bold/italic/lists only. Rendered by a simple in-app parser. */
    val body: String,
    val vocabRefs: List<String> = emptyList(),
    val examples: List<GrammarExample> = emptyList()
)

@Serializable
data class GrammarExample(
    val sr: String,
    val en: String
)

@Serializable
data class GrammarIndex(
    val schemaVersion: Int,
    val notes: List<String>              // file names to load from assets/grammar/
)
