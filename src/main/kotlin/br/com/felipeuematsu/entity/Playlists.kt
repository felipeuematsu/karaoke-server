package br.com.felipeuematsu.entity

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDate
import java.time.LocalDateTime

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
    var lastUpdated by Playlists.lastUpdated
}

object Playlists : IntIdTable("playlists") {

    val name = varchar("name", 255)
    val userName = varchar("user_name", 255)
    val description = varchar("description", 255).nullable()
    val imageUrl = varchar("image_url", 1023).nullable()
    val lastUpdated = datetime("last_updated").default(LocalDateTime.now())
}