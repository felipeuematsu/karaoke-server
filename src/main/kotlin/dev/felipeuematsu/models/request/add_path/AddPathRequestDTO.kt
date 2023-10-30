package dev.felipeuematsu.models.request.add_path

import kotlinx.serialization.Serializable

@Serializable
data class AddPathRequestDTO(
    val path: String? = null,
    val regex: String? = null,
    val titlePos: Int = 1,
    val artistPos: Int = 2,
)
