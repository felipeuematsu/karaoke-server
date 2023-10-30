package dev.felipeuematsu.entity

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

@Serializable
data class HistorySongDTO(
    val historySinger: Int,
    val filepath: String,
    val artist: String,
    val title: String,
    val songid: String,
    val keychange: Int,
    val plays: Int,
    @Serializable(with = InstantSerializer::class)
    val lastPlay: Instant,
)

class HistorySong(id: EntityID<Int>) : IntEntity(id) {
    fun toDto() = HistorySongDTO(
        historySinger = historySinger,
        filepath = filepath,
        artist = artist,
        title = title,
        songid = songid,
        keychange = keychange,
        plays = plays,
        lastPlay = lastPlay,
    )
    companion object : IntEntityClass<HistorySong>(HistorySongs)

    var historySinger by HistorySongs.historySinger
    var filepath by HistorySongs.filepath
    var artist by HistorySongs.artist
    var title by HistorySongs.title
    var songid by HistorySongs.songid
    var keychange by HistorySongs.keychange
    var plays by HistorySongs.plays
    var lastPlay by HistorySongs.lastPlay

}

object HistorySongs : IntIdTable("historySongs") {
    val historySinger = integer("historySinger")
    val filepath = text("filepath")
    val artist = text("artist")
    val title = text("title")
    val songid = text("songid")
    val keychange = integer("keychange").default(0)
    val plays = integer("plays").default(0)
    val lastPlay = timestamp("lastplay")
}