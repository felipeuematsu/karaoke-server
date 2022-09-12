package br.com.felipeuematsu.entity

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant


@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = Instant::class)
object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): Instant {
        return Instant.parse(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }
}

@Serializable
data class SongDTO(
    val songId: Int,
    val title: String,
    val artist: String,
    val discId: String? = null,
    val path: String,
    val filename: String,
    val searchString: String,
    val duration: Int,
    val plays: Int,
    @Serializable(with = InstantSerializer::class)
    val lastPlayed: Instant?,
    var imageUrl: String? = null,
)

class Song(songId: EntityID<Int>) : IntEntity(songId) {
    fun toDTO() = SongDTO(id.value, title, artist, discId, path, filename, searchString, duration, plays, lastPlayed)

    companion object : IntEntityClass<Song>(DBSongs)

    var title by DBSongs.title
    var artist by DBSongs.artist
    var duration by DBSongs.duration
    var plays by DBSongs.plays
    var lastPlayed by DBSongs.lastPlay
    var discId by DBSongs.discId
    var path by DBSongs.path
    var filename by DBSongs.filename
    var searchString by DBSongs.searchString

}


object DBSongs : IntIdTable("dbSongs", "songid") {
    val title = text("Title")
    val artist = text("Artist")
    val discId = text("DiscId").nullable()
    val duration = integer("Duration")
    val path = varchar("path", 700).index("idx_path", true)
    val filename = text("filename")
    val searchString = text("searchstring")
    val plays = integer("plays").default(0)
    val lastPlay = timestamp("lastplay").nullable()
}