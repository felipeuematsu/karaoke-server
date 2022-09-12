package br.com.felipeuematsu.entity

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

@Serializable
data class PlaylistDTO(
    val id: Int,
    val name: String,
    val songs: List<SongDTO> = listOf(),
    val imageUrl: String?,
    val description: String?,
)

class Playlist(id: EntityID<Int>) : IntEntity(id) {
    fun toDTO() = PlaylistDTO(id.value, name, songs.map(Song::toDTO), imageUrl, description)

    companion object : IntEntityClass<Playlist>(Playlists)

    var name by Playlists.name
    var userName by Playlists.userName
    var description by Playlists.description
    var songs by Song via PlaylistSongs
    var imageUrl by Playlists.imageUrl
}

class SimplePlaylist(id: EntityID<Int>) : IntEntity(id) {
    fun toDTO() = PlaylistDTO(
        id = id.value,
        name = name,
        imageUrl = imageUrl,
        description = description
    )

    companion object : IntEntityClass<SimplePlaylist>(Playlists)

    var name by Playlists.name
    var description by Playlists.description
    var imageUrl by Playlists.imageUrl
}

object Playlists : IntIdTable("playlists") {
    val name = varchar("name", 255)
    val userName = varchar("user_name", 255)
    val description = varchar("description", 255).nullable()
    val imageUrl = varchar("image_url", 1023).nullable()
}