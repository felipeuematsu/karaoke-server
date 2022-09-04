package br.com.felipeuematsu.entity

import org.jetbrains.exposed.sql.Table

object PlaylistSongs: Table("playlist_songs") {
    val playlistId = integer("playlist_id").references(Playlists.id)
    val songId = integer("song_id").references(DBSongs.id)

    override val primaryKey = PrimaryKey(playlistId, songId)
}