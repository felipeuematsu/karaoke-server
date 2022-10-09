package br.com.felipeuematsu.models.dao.impl

import br.com.felipeuematsu.database.KaraokeDatabase.dbQuery
import br.com.felipeuematsu.entity.Song
import br.com.felipeuematsu.entity.SongDTO
import br.com.felipeuematsu.entity.DBSongs
import br.com.felipeuematsu.entity.SongResponseDTO
import br.com.felipeuematsu.models.dao.SongDAO
import br.com.felipeuematsu.service.SpotifyService
import br.com.felipeuematsu.service.forEachParallel
import org.jetbrains.exposed.sql.*
import java.time.Instant

class SongDAOImpl : SongDAO {
    private fun resultRowToSong(row: ResultRow) = Song.wrapRow(row).toDTO()

    override suspend fun allSongs(): List<SongDTO> = dbQuery {
        DBSongs.selectAll().map(::resultRowToSong).toList()
    }

    override suspend fun getSong(id: Int): SongDTO? = dbQuery {
        Song.find { DBSongs.id eq id }.firstOrNull()?.toDTO()
    }

    override suspend fun addNewSong(
        title: String,
        artist: String,
        duration: Int,
        plays: Int,
        lastPlayed: Instant?,
    ): SongDTO = dbQuery {
        Song.new {
            this.title = title
            this.artist = artist
            this.duration = duration
            this.plays = plays
            this.lastPlayed = lastPlayed
        }.toDTO()
    }

    override suspend fun addNewSongIgnore(
        title: String,
        artist: String,
        duration: Int,
        plays: Int,
        lastPlayed: Instant?,
    ): SongDTO = dbQuery {
        Song.new {
            this.artist = artist
            this.title = title
            this.duration = duration
            this.plays = plays
            this.lastPlayed = lastPlayed
        }.toDTO()
    }

    override suspend fun editSong(
        songId: Int,
        title: String,
        artist: String,
        duration: Int,
        plays: Int,
        lastPlayed: Instant?,
    ): Boolean = dbQuery {
        DBSongs.update({ DBSongs.id eq songId }) {
            it[DBSongs.id] = songId
            it[DBSongs.artist] = artist
            it[DBSongs.title] = title
            it[DBSongs.duration] = duration
            it[DBSongs.plays] = plays
            it[lastPlay] = lastPlayed
        } > 0
    }

    override suspend fun deleteSong(id: Int): Boolean = dbQuery { DBSongs.deleteWhere { DBSongs.id eq id } > 0 }

    override suspend fun searchSong(title: String?, artist: String?, page: Int, perPage: Int): SongResponseDTO =
        dbQuery {
            val select =
                if (title != null && artist != null) {
                    DBSongs.select { DBSongs.title like "%$title%" and (DBSongs.artist like "%$artist%") }
                } else if (title != null) {
                    DBSongs.select { DBSongs.title like "%$title%" }
                } else if (artist != null) {
                    DBSongs.select { DBSongs.artist like "%$artist%" }
                } else {
                    null
                }
            val total = select?.count() ?: 0
            val songs = select
                ?.limit(perPage, ((page - 1) * perPage).toLong())
                ?.map(::resultRowToSong)?.toList()
                ?: emptyList()
            songs.map {
                it.apply {
                    imageUrl =
                        SpotifyService.searchSongImage(song = it.title, artist = it.artist)
                            ?: SpotifyService.searchArtistImages(it.artist)
                }
            }.toList()

            SongResponseDTO(
                page = page,
                data = songs,
                total = total.toInt(),
                perPage = perPage,
                totalPages = (total / perPage + 1).toInt()
            )
        }

    override suspend fun deleteAllSongs(): Int = dbQuery {
        DBSongs.deleteAll()
    }

}