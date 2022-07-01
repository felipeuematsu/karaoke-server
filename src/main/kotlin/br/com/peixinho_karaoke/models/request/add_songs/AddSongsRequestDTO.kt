package br.com.peixinho_karaoke.models.request.add_songs

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class AddSongsRequestDTO(
    val songs: List<SongDTO>? = null,
    val command: String,
    @SerialName("system_id")
    val systemId: Int? = 0,
)
