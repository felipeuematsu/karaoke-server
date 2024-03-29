package dev.felipeuematsu.models.response

import dev.felipeuematsu.entity.SongDTO
import kotlinx.serialization.Serializable

@Serializable
data class CurrentSongDTO(
    val song: SongDTO? = null,
    val songId: Int? = null,
    val singer: String? = null,
    val position: Int,
    val isPlaying: Boolean,
)