package br.com.peixinho_karaoke.models.spotify

import kotlinx.serialization.Serializable

@Serializable
data class SearchResponseDTO(
    val tracks: TracksDTO? = null,
    val artists: ArtistsDTO? = null,
    val albums: AlbumsDTO? = null,
    val playlists: PlaylistsDTO? = null,

    )

@Serializable
data class PlaylistsDTO(
    val items: List<SpotifyPlaylistDTO>? = null,
    val total: Int? = null,
    val limit: Int? = null,
    val offset: Int? = null,
    val href: String? = null,
    val previous: String? = null,
    val next: String? = null,
)

@Serializable
data class SpotifyPlaylistDTO(
    val collaborative: Boolean? = null,
    val description: String? = null,
    val external_urls: ExternalUrlsDTO? = null,
    val href: String? = null,
    val id: String? = null,
    val images: List<ImageDTO>? = null,
    val name: String? = null,
    val owner: OwnerDTO? = null,
    val public: Boolean? = null,
    val snapshot_id: String? = null,
    val tracks: TracksDTO? = null,
    val type: String? = null,
    val uri: String? = null
)

@Serializable
data class OwnerDTO(
    val display_name: String? = null,
    val external_urls: ExternalUrlsDTO? = null,
    val href: String? = null,
    val id: String? = null,
    val type: String? = null,
    val uri: String? = null,
)

@Serializable
data class AlbumsDTO(
    val items: List<AlbumDTO>? = null, val total: Int? = null,
)

@Serializable
data class TracksDTO(
    val items: List<TrackDTO>? = null, val total: Int? = null,
)

@Serializable
data class TrackDTO(
    val id: String? = null,
    val name: String? = null,
    val artists: List<ArtistDTO>? = null,
    val album: AlbumDTO? = null,
    val external_urls: ExternalUrlsDTO? = null,
    val href: String? = null,
    val uri: String? = null,
)

@Serializable
data class AlbumDTO(
    val id: String? = null,
    val name: String? = null,
    val images: List<ImageDTO>? = null,
    val external_urls: ExternalUrlsDTO? = null,
    val href: String? = null,
    val uri: String? = null,
)

@Serializable
data class ArtistsDTO(
    val href: String? = null,
    val items: List<ArtistDTO>? = null,
    val limit: Int? = null,
    val next: String? = null,
    val offset: Int? = null,
    val previous: String? = null,
    val total: Int? = null
)

@Serializable
data class ArtistDTO(
    val external_urls: ExternalUrlsDTO? = null,
    val followers: FollowersDTO? = null,
    val genres: List<String>? = null,
    val href: String? = null,
    val id: String? = null,
    val images: List<ImageDTO>? = null,
    val name: String? = null,
    val popularity: Int? = null,
    val type: String? = null,
    val uri: String? = null

)

@Serializable
data class ImageDTO(
    val height: Int? = null,
    val url: String? = null,
    val width: Int? = null,
)

@Serializable
data class FollowersDTO(
    val href: String? = null, val total: Int? = null
)

@Serializable
data class ExternalUrlsDTO(
    val spotify: String? = null
)