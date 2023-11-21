package dev.felipeuematsu.models.youtube

import kotlinx.serialization.Serializable

@Serializable
data class YoutubeManifest(
    val resolution: Resolution,
    val stream: String,
)

@Serializable
data class Resolution(
    val width: Int,
    val height: Int,
)