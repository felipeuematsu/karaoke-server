package br.com.peixinho_karaoke.models.request.add_songs

import kotlinx.serialization.Serializable

@Serializable
data class SongDTO(
    val artist: String? = null,
    val title: String? = null,
    val duration: Int? = -1,
)
