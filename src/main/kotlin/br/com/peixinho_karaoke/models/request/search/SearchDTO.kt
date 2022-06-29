package br.com.peixinho_karaoke.models.request.search

import kotlinx.serialization.Serializable

@Serializable
class SearchDTO(
    val searchString: String?,
)