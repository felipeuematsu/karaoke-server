package dev.felipeuematsu.models.request.add_songs

import kotlinx.serialization.Serializable

@Serializable
data class AddSongsResponseDTO(
    val command: String,
    val error: String,
    val errors: List<String>,
    val last_artist: String?,
    val last_title: String?,
    val `entries processed`: Int,
)