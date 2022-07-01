package br.com.peixinho_karaoke.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

@Serializable
data class Request(
    @SerialName("request_id")
    val requestId: Int,
    val title: String,
    val artist: String,
    val singer: String,
    val keyChange: Int,
    @Serializable(with = LocalDateTimeSerializer::class)
    @SerialName("request_time")
    val requestTime: LocalDateTime,
)

object Requests : Table("requests") {
    val requestId = integer("request_id").autoIncrement()
    val title = text("title")
    val artist = text("artist")
    val singer = text("singer")
    val keyChange = integer("key_change")
    val requestTime = datetime("request_time")
    override val primaryKey = PrimaryKey(requestId)
}