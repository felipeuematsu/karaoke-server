package br.com.peixinho_karaoke.models.request.add_songs

import br.com.peixinho_karaoke.models.InstantSerializer
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class NewSongDTO(
    val artist: String? = null,
    val title: String? = null,
    val duration: Int? = null,
    val plays: Int? = null,
    @Serializable(with = InstantSerializer::class)
    val lastPlayed: Instant? = null,
)
