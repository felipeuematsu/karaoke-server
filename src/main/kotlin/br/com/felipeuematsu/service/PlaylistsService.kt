package br.com.felipeuematsu.service

import br.com.felipeuematsu.entity.*
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object PlaylistsService {

    private fun updateTop50(): PlaylistDTO = transaction {
        val top50 = Playlist
            .find { Playlists.name eq "Top 50" }
            .firstOrNull() ?: Playlist.new { name = "Top 50"; userName = "default" }
        top50.songs = SizedCollection(
            DBSongs
                .select { DBSongs.plays greater 0 }
                .orderBy(DBSongs.plays to SortOrder.DESC)
                .limit(50)
                .sortedBy {
                    it[DBSongs.plays]
                    it[DBSongs.lastPlay]
                }
                .map(Song::wrapRow)
        )
        top50.toDTO()
    }

    private fun updateLast50(): PlaylistDTO = transaction {
        val last50 = Playlist
            .find { Playlists.name eq "Last 50" }
            .firstOrNull() ?: Playlist.new { name = "Last 50"; userName = "default" }
        last50.songs = SizedCollection(
            DBSongs
                .select { DBSongs.plays greater 0 }
                .orderBy(DBSongs.lastPlay to SortOrder.DESC)
                .limit(50)
                .sortedBy { it[DBSongs.lastPlay] }
                .map(Song::wrapRow)
        )
        last50.toDTO()
    }


    fun updateDefaultPlaylists(): List<PlaylistDTO> {
        return mutableListOf(updateTop50(), updateLast50()).apply { addAll(updateTop5Artists()) }
    }

    private fun updateTop5Artists(): MutableList<PlaylistDTO> = transaction {

        val top5Artists = DBSongs
            .select { DBSongs.plays greater 0 }
            .groupBy(DBSongs.artist)
            .orderBy(DBSongs.plays to SortOrder.DESC)
            .limit(5)
            .map(Song::wrapRow)
            .map { it.artist }

        print(top5Artists)
        val response = mutableListOf<PlaylistDTO>()

        top5Artists.forEach {
            transaction {
                val artistPlaylist =
                    Playlist.find { Playlists.name eq it }.firstOrNull() ?: Playlist.new {
                        name = it; userName = "default"
                    }
                artistPlaylist.songs = SizedCollection(
                    DBSongs.select { DBSongs.artist eq it }
                        .orderBy(DBSongs.plays to SortOrder.DESC)
                        .limit(50)
                        .sortedBy {
                            it[DBSongs.plays]
                            it[DBSongs.lastPlay]
                        }
                        .map(Song::wrapRow)
                )
                if (artistPlaylist.imageUrl == null) {
                    artistPlaylist.imageUrl = SpotifyService.searchArtistImages(it)
                }
                response.add(artistPlaylist.toDTO())
            }

        }
        return@transaction response
    }

    fun getPlaylists(): List<PlaylistDTO> = transaction {
        listOf(
            Playlist.find { Playlists.name eq "Top 50" }.firstOrNull()?.toDTO(),
            Playlist.find { Playlists.name eq "Last 50" }.firstOrNull()?.toDTO(),
            *Playlist.find { Playlists.name notInList listOf("Top 50", "Last 50") }
                .limit(5)
                .map { it.toDTO() }.toList().toTypedArray()
        ).requireNoNulls()
    }

    fun getPlaylist(id: Int): PlaylistDTO? = transaction {
        Playlist.findById(id)?.toDTO()?.apply {
            songs.map {
                it.imageUrl =
                    SpotifyService.searchSongImage(it.title, it.artist) ?: SpotifyService.searchArtistImages(it.artist)
            }
        }
    }
}