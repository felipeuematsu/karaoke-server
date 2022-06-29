package br.com.peixinho_karaoke.models

import org.jetbrains.exposed.sql.Table

data class Song(
    val songId: Int,
    val title: String,
    val artist: String,
    val combined: String,
)


object Songs : Table("songdb") {
    val songId = integer("song_id").autoIncrement()
    val title = text("title")
    val artist = text("artist")
    val combined = text("combined").index("idx_songstrings", true)

    override val primaryKey = PrimaryKey(songId)
}