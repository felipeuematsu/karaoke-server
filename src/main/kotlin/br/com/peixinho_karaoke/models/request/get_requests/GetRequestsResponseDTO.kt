package br.com.peixinho_karaoke.models.request.get_requests

import br.com.peixinho_karaoke.models.Request
import kotlinx.serialization.Serializable

@Serializable
data class GetRequestsResponseDTO(
    val requests: List<Request>,
    val command: String,
    val error: String,
    val serial: Int,
)
