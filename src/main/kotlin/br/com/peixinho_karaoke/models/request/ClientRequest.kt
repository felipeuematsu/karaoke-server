package br.com.peixinho_karaoke.models.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClientRequest(
    val command: String,
    @SerialName("api_key") val apiKey: String,
    val singerName: String?,
    val songId: Int?,
    val searchString: String?,
    val request_id: Int?,
    val songs:
)
