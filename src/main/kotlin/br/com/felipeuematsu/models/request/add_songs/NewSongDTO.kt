package br.com.felipeuematsu.models.request.add_songs

import br.com.felipeuematsu.entity.InstantSerializer
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
