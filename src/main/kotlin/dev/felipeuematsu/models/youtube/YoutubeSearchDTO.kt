package dev.felipeuematsu.models.youtube

import dev.felipeuematsu.models.LocalDateTimeSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Duration
import java.time.LocalDateTime

@Serializable
data class YoutubeSearchDTO(
    val uuid: String,
    val content: List<VideoDto>,
    @Serializable(with = LocalDateTimeSerializer::class)
    val expiration: LocalDateTime,
)

@Serializable
data class VideoDto(
    val id: String?,
    val title: String?,
    val author: String?,
    val description: String?,
    @Serializable(with = DurationSerializer::class)
    val duration: Duration?,
    val viewCount: Long?,
    val thumbnails: Thumbnails?,
    @Serializable(with = LocalDateTimeSerializer::class)
    val uploadDate: LocalDateTime?,

    )

@Serializable

data class Thumbnails(
    val lowResUrl: String?,
    val mediumResUrl: String?,
    val highResUrl: String?,
    val standardResUrl: String?,
    val maxResUrl: String?,
)

class DurationSerializer : KSerializer<Duration> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.LONG
        )

    override fun deserialize(decoder: Decoder): Duration {
        val duration = decoder.decodeLong()
        return Duration.ofNanos(duration)
    }

    override fun serialize(encoder: Encoder, value: Duration) {
        encoder.encodeLong(value.toNanos())
    }

}