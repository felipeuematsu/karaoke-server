package dev.felipeuematsu.models.dao.impl

import dev.felipeuematsu.database.KaraokeDatabase.dbQuery
import dev.felipeuematsu.entity.DBSongs
import dev.felipeuematsu.entity.Song
import dev.felipeuematsu.entity.SongDTO
import dev.felipeuematsu.entity.SongResponseDTO
import dev.felipeuematsu.models.dao.SongDAO
import dev.felipeuematsu.service.SpotifyService
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
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
                ?.orderBy(DBSongs.plays to SortOrder.DESC)
                ?.limit(perPage, ((page - 1) * perPage).toLong())
                ?.map(::resultRowToSong)?.toList()
                ?: emptyList()
            songs.map { it.apply { imageUrl = SpotifyService.searchSongImage(song = it.title, artist = it.artist) } }
                .toList()

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