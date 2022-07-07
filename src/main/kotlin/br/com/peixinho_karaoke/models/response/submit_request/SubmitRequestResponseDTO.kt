package br.com.peixinho_karaoke.models.response.submit_request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubmitRequestResponseDTO(
    @SerialName("error")
    val error: String = "false",
    @SerialName("command")
    val command: String = "submitRequest",
    @SerialName("request_id")
    val requestId: Int? = -1,
)
