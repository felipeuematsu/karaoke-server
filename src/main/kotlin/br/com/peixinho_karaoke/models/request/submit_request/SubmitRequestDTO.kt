package br.com.peixinho_karaoke.models.request.submit_request

import kotlinx.serialization.Serializable

@Serializable
data class SubmitRequestDTO(
    val songId: Int?,
    val singerName: String?,
    val keyChange: Int?,
)