package br.com.peixinho_karaoke.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

data class Request(
    val requestId: Int,
    val title: String,
    val artist: String,
    val singer: String,
    val requestTime: LocalDateTime
)

object Requests : Table("requests") {
    val requestId = integer("request_id").autoIncrement()
    val title = text("title")
    val artist = text("artist")
    val singer = text("singer")
    val requestTime = datetime("request_time")

    override val primaryKey = PrimaryKey(requestId)
}