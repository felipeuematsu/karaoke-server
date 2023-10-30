package dev.felipeuematsu.models

import kotlinx.serialization.Serializable

@Serializable
data class YoutubeSongDTO(
    val url: String? = null,
    val title: String? = null,
    val artist: String? = null,
    val thumbnail: String? = null,
    val duration: Int? = null,
)