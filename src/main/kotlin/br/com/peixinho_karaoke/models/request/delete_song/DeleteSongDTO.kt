package br.com.peixinho_karaoke.models.request.delete_song

import kotlinx.serialization.Serializable

@Serializable
data class DeleteSongDTO(
    val request_id: Int?,
)
