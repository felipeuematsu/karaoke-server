package dev.felipeuematsu.models.request

import kotlinx.serialization.Serializable

@Serializable
data class VolumeRequestDTO(
    val volume: Int
)
