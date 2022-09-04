package br.com.felipeuematsu.entity

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

@Serializable
data class QueueSongDTO(
    val singer: SingerDTO?,
    val song: SongDTO?,
    val position: Int?,
    val keyChange: Int?
)

class QueueSong(qsongid: EntityID<Int>) : IntEntity(qsongid) {
    fun toDTO(): QueueSongDTO {
        return QueueSongDTO(Singer.findById(singer)?.toDTO(), Song.findById(song)?.toDTO(), position, keyChange)
    }

    companion object : IntEntityClass<QueueSong>(QueueSongs)

    var singer by QueueSongs.singerId
    var song by QueueSongs.songId
    var position by QueueSongs.position
    var keyChange by QueueSongs.keyChange
}

object QueueSongs : IntIdTable("queueSongs", "qsongid") {
    val singerId = integer("singer")
    val songId = integer("song")
    val position = integer("position")
    val keyChange = integer("keyChange")

}
