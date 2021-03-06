package br.com.peixinho_karaoke.models.spotify

import kotlinx.serialization.Serializable

@Serializable
data class TokenResponseDTO(
    val access_token: String? = null,
    val token_type: String? = null,
    val expires_in: Long? = null,
)
