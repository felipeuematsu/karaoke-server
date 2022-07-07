package br.com.peixinho_karaoke.models

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
    val duration: Int,
    val plays: Int,
    @Serializable(with = InstantSerializer::class)
    val lastPlayed: Instant?,
    val combined: String,
)

class Song(songId: EntityID<Int>) : IntEntity(songId) {
    fun toDTO() = SongDTO(id.value, title, artist, duration, plays, lastPlayed, combined)

    companion object : IntEntityClass<Song>(Songs)

    var title by Songs.title
    var artist by Songs.artist
    var duration by Songs.duration
    var plays by Songs.plays
    var lastPlayed by Songs.lastPlayed
    var combined by Songs.combined
}


object Songs : IntIdTable("songdb", "song_id") {
    val title = text("title")
    val artist = text("artist")
    val duration = integer("duration")
    val plays = integer("plays")
    val lastPlayed = timestamp("lastPlayed").nullable()
    val combined = text("combined").index("idx_songstrings", true)
}