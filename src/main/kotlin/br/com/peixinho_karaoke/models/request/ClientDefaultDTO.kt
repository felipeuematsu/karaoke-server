package br.com.peixinho_karaoke.models.request

import br.com.peixinho_karaoke.models.request.add_songs.NewSongDTO
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClientDefaultDTO(
    val command: String,
    @SerialName("api_key")
    val apiKey : String,
    val singerName: String? = null,
    val songId: Int? = null,
    val searchString: String? = null,
    val request_id: Int? = null,
    val songs: List<NewSongDTO>? = null,
    val accepting: Boolean? = null,
    val venue_id: Int? = null,
    val system_id: Int? = null,
)

