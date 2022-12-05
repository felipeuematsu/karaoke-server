package br.com.felipeuematsu.models.request

import kotlinx.serialization.Serializable

@Serializable
data class QueueReorderRequestDTO(
    val queueSongId: Int?,
    val newIndex: Int?,
)
