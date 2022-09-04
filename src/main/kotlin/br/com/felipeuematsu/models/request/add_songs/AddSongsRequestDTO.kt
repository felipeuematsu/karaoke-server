package br.com.felipeuematsu.models.request.add_songs

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class AddSongsRequestDTO(
    val songs: List<NewSongDTO>? = null,
    val command: String,
    @SerialName("system_id")
    val systemId: Int? = 0,
)
