package br.com.peixinho_karaoke.models.response

import kotlinx.serialization.Serializable

@Serializable
data class GetSerialResponse(
    val serial: Int,
    val error: String = "false",
    val command: String,
)