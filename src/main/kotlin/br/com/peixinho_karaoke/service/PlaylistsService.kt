package br.com.peixinho_karaoke.service

import br.com.peixinho_karaoke.models.*
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object PlaylistsService {

    private fun updateTop50(): PlaylistDTO = transaction {
        val top50 = Playlist
            .find { Playlists.name eq "Top 50" }
            .firstOrNull() ?: Playlist.new { name = "Top 50" }
        top50.songs = SizedCollection(
            Songs
                .select { Songs.plays greater 0 }
                .orderBy(Songs.plays to SortOrder.DESC)
                .limit(50)
                .map(Song::wrapRow)
        )
        top50.toDTO()
    }

    private fun updateLast50(): PlaylistDTO = transaction {
        val last50 = Playlist
            .find { Playlists.name eq "Last 50" }
            .firstOrNull() ?: Playlist.new { name = "Last 50" }
        last50.songs = SizedCollection(
            Songs
                .select { Songs.plays greater 0 }
                .orderBy(Songs.lastPlayed to SortOrder.DESC)
                .limit(50).map(Song::wrapRow)
        )
        last50.toDTO()
    }


    fun updateDefaultPlaylists(): List<PlaylistDTO> {
        return mutableListOf(updateTop50(), updateLast50()).apply { addAll(updateTop5Artists()) }
    }


    private fun updateTop5Artists(): MutableList<PlaylistDTO> = transaction {

        val top5Artists = Songs
            .select { Songs.plays greater 0 }
            .groupBy(Songs.artist)
            .orderBy(Songs.plays to SortOrder.DESC)
            .limit(5)
            .map(Song::wrapRow)
            .map { it.artist }

        print(top5Artists)
        val response = mutableListOf<PlaylistDTO>()

        top5Artists.forEach {
            transaction {
                val artistPlaylist =
                    Playlist.find { Playlists.name eq it }.firstOrNull() ?: Playlist.new { name = it }
                artistPlaylist.songs = SizedCollection(
                    Songs.select { Songs.artist eq it }
                        .orderBy(Songs.plays to SortOrder.DESC)
                        .limit(50)
                        .map(Song::wrapRow)
                )
                if (artistPlaylist.imageUrl == null) {
                    artistPlaylist.imageUrl = SpotifyService.searchImages(it)
                }
                response.add(artistPlaylist.toDTO())
            }

        }
        return@transaction response
    }
}