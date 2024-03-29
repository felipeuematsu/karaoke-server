package dev.felipeuematsu.service

import dev.felipeuematsu.entity.DBSongs
import dev.felipeuematsu.entity.Playlist
import dev.felipeuematsu.entity.PlaylistDTO
import dev.felipeuematsu.entity.Playlists
import dev.felipeuematsu.entity.Song
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.ZoneOffset

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
                .sortedByDescending { it[DBSongs.lastPlay]; it[DBSongs.plays] }
                .map(Song::wrapRow)
        )
        top50.lastUpdated = LocalDateTime.now().toInstant(ZoneOffset.UTC)
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
        last50.lastUpdated = LocalDateTime.now().toInstant(ZoneOffset.UTC)
        last50.toDTO()
    }


    fun updateDefaultPlaylists(): List<PlaylistDTO> {
        return mutableListOf(updateTop50(), updateLast50()).apply { addAll(updateTop5Artists()) }
    }

    private fun updateTop5Artists(): MutableList<PlaylistDTO> = transaction {

        val top5Artists = DBSongs
            .selectAll()
            .groupBy(DBSongs.artist)
            .orderBy(DBSongs.plays to SortOrder.DESC)
            .limit(5)
            .map(Song::wrapRow)
            .map { it.artist }

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
                        .sortedByDescending { it[DBSongs.lastPlay]; it[DBSongs.plays] }
                        .map(Song::wrapRow)
                )
                artistPlaylist.lastUpdated = LocalDateTime.now().toInstant(ZoneOffset.UTC)
                if (artistPlaylist.imageUrl == null) {
                    artistPlaylist.imageUrl = SpotifyService.searchArtistImages(it)
                }
                response.add(artistPlaylist.toDTO())
            }

        }
        return@transaction response
    }

    fun getPlaylists(): List<PlaylistDTO> = transaction {
        val top50 = Playlist.find { Playlists.name eq "Top 50" }.firstOrNull()?.toDTO()
        val last50 = Playlist.find { Playlists.name eq "Last 50" }.firstOrNull()?.toDTO()
        listOfNotNull(
            top50?.copy(songs = top50.songs.sortedByDescending { it.lastPlayed; it.plays }.toList()),
            last50?.copy(songs = last50.songs.sortedByDescending { it.lastPlayed }.toList()),
            *Playlist.find { Playlists.name notInList listOf("Top 50", "Last 50") }
                .sortedByDescending { it.songs.count(); it.lastUpdated }
                .map(Playlist::toDTO).take(5).toList().toTypedArray()
        )
    }

    fun getPlaylist(id: Int): PlaylistDTO? = transaction {
        var playlistDTO = Playlist.findById(id)?.toDTO()
        playlistDTO = if (playlistDTO?.name == "Last 50") {
            val songs = playlistDTO.songs.apply { sortedByDescending { it.lastPlayed } }
            playlistDTO.copy(songs = songs)
        } else {
            val songs = playlistDTO?.songs?.sortedByDescending { it.lastPlayed; it.plays }
            songs?.let { playlistDTO?.copy(songs = it) }
        }
        playlistDTO?.apply {
            songs.map {
                it.imageUrl =
                    SpotifyService.searchSongImage(it.title, it.artist)
                        ?: SpotifyService.searchArtistImages(it.artist)
            }
        }
    }
}