package br.com.peixinho_karaoke.models.dao.impl

import br.com.peixinho_karaoke.database.RequestsDatabaseFactory.dbQuery
import br.com.peixinho_karaoke.models.Song
import br.com.peixinho_karaoke.models.Songs
import br.com.peixinho_karaoke.models.dao.SongDAO
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like

class SongDAOImpl : SongDAO {
    private fun resultRowToSong(row: ResultRow) = Song(
        songId = row[Songs.songId],
        artist = row[Songs.artist],
        title = row[Songs.title],
        duration = row[Songs.duration],
        combined = row[Songs.combined],
    )

    override suspend fun allSongs(): List<Song> = dbQuery {
        Songs.selectAll().map(::resultRowToSong).toList()
    }

    override suspend fun getSong(id: Int): Song? = dbQuery {
        Songs.select { Songs.songId eq id }
            .map(::resultRowToSong)
            .singleOrNull()
    }

    override suspend fun addNewSong(
        songId: Int,
        title: String,
        artist: String,
        duration: Int,
        combined: String
    ): Song? = dbQuery {
        val insertSongment = Songs.insert {
            it[Songs.songId] = songId
            it[Songs.artist] = artist
            it[Songs.title] = title
            it[Songs.duration] = duration
            it[Songs.combined] = combined
        }
        insertSongment.resultedValues?.map(::resultRowToSong)?.singleOrNull()
    }

    override suspend fun addNewSongIgnore(
        songId: Int,
        title: String,
        artist: String,
        duration: Int,
        combined: String
    ): Song? =
        dbQuery {
            val insertSongment = Songs.insertIgnore {
                it[Songs.songId] = songId
                it[Songs.artist] = artist
                it[Songs.title] = title
                it[Songs.duration] = duration
                it[Songs.combined] = combined
            }
            insertSongment.resultedValues?.map(::resultRowToSong)?.singleOrNull()
        }

    override suspend fun editSong(
        songId: Int,
        title: String,
        artist: String,
        duration: Int,
        combined: String
    ): Boolean = dbQuery {
        Songs.update({ Songs.songId eq songId }) {
            it[Songs.songId] = songId
            it[Songs.artist] = artist
            it[Songs.title] = title
            it[Songs.duration] = duration
            it[Songs.combined] = combined
        } > 0
    }

    override suspend fun deleteSong(id: Int): Boolean = dbQuery {
        Songs.deleteWhere { Songs.songId eq id } > 0
    }

    override suspend fun searchSong(searchString: String): List<Song> = dbQuery {
        Songs.select(Songs.combined.like("%$searchString%")).map(::resultRowToSong).toList()
    }

}