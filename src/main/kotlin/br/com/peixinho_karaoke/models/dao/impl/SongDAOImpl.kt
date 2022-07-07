package br.com.peixinho_karaoke.models.dao.impl

import br.com.peixinho_karaoke.database.RequestsDatabaseFactory.dbQuery
import br.com.peixinho_karaoke.models.Song
import br.com.peixinho_karaoke.models.SongDTO
import br.com.peixinho_karaoke.models.Songs
import br.com.peixinho_karaoke.models.dao.SongDAO
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import java.time.Instant

class SongDAOImpl : SongDAO {
    private fun resultRowToSong(row: ResultRow) = Song.wrapRow(row).toDTO()

    override suspend fun allSongs(): List<SongDTO> = dbQuery {
        Songs.selectAll().map(::resultRowToSong).toList()
    }

    override suspend fun getSong(id: Int): SongDTO? = dbQuery {
        Song.find { Songs.id eq id }.firstOrNull()?.toDTO()
    }

    override suspend fun addNewSong(
        title: String,
        artist: String,
        duration: Int,
        plays: Int,
        combined: String,
        lastPlayed: Instant?,
    ): SongDTO = dbQuery {
        Song.new {
            this.title = title
            this.artist = artist
            this.duration = duration
            this.plays = plays
            this.combined = combined
            this.lastPlayed = lastPlayed
        }.toDTO()
    }

    override suspend fun addNewSongIgnore(
        title: String,
        artist: String,
        duration: Int,
        plays: Int,
        combined: String,
        lastPlayed: Instant?,

        ): SongDTO = dbQuery {
        Song.new {
            this.artist = artist
            this.title = title
            this.duration = duration
            this.plays = plays
            this.combined = combined
            this.lastPlayed = lastPlayed
        }.toDTO()
    }

    override suspend fun editSong(
        songId: Int,
        title: String,
        artist: String,
        duration: Int,
        plays: Int,
        combined: String,
        lastPlayed: Instant?,
    ): Boolean = dbQuery {
        Songs.update({ Songs.id eq songId }) {
            it[Songs.id] = songId
            it[Songs.artist] = artist
            it[Songs.title] = title
            it[Songs.duration] = duration
            it[Songs.plays] = plays
            it[Songs.combined] = combined
            it[Songs.lastPlayed] = lastPlayed
        } > 0
    }

    override suspend fun deleteSong(id: Int): Boolean = dbQuery { Songs.deleteWhere { Songs.id eq id } > 0 }

    override suspend fun searchSong(searchString: String): List<SongDTO> = dbQuery {
        Songs.select(Songs.combined.like("%$searchString%")).map(::resultRowToSong).toList()
    }

    override suspend fun deleteAllSongs(): Int = dbQuery {
        Songs.deleteAll()
    }

}