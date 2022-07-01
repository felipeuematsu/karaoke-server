package br.com.peixinho_karaoke.models.request.delete_request

import kotlinx.serialization.Serializable

@Serializable
data class DeleteRequestDTO(
    val request_id: Int?,
)
