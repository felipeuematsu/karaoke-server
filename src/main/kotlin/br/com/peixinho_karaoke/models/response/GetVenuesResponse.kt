package br.com.peixinho_karaoke.models.response

import kotlinx.serialization.Serializable

@Serializable
data class GetVenuesResponse(
    val venues: List<VenueResponse>,
    val err: String = "false",
    val com: String,
)

@Serializable
data class VenueResponse(
    val venue_id: Int,
    val accepting: Int,
    val name: String,
    val url_name: String,
)