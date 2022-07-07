package br.com.peixinho_karaoke.models

import org.jetbrains.exposed.sql.Table

object PlaylistSongs: Table("playlist_songs") {
    val playlistId = integer("playlist_id").references(Playlists.id)
    val songId = integer("song_id").references(Songs.id)

    override val primaryKey = PrimaryKey(playlistId, songId)
}