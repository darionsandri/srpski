package com.srpski.app.data.model

import kotlinx.serialization.Serializable

/** Top-level of `packs/index.json` served from the GitHub Pages content host. */
@Serializable
data class PackIndex(
    val schemaVersion: Int,
    val packs: List<PackSummary>
)

@Serializable
data class PackSummary(
    val id: String,
    val title: String,
    val cefr: String,
    val version: Int,
    val sizeBytes: Long,
    val sha256: String,
    val requires: List<String> = emptyList(),
    val minAppVersion: Int = 1,
    val url: String,                         // absolute URL to manifest.json
    val topics: List<String> = emptyList()   // topic ids used for interest routing
)

/** `packs/<id>/manifest.json` payload. Mirrors CourseFile+units for merging. */
@Serializable
data class PackManifest(
    val id: String,
    val version: Int,
    val title: String,
    val units: List<UnitDef>,
    val unitFiles: List<String>              // relative unitNN.json paths
)
