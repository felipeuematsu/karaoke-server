package br.com.felipeuematsu.models.dao

import br.com.felipeuematsu.entity.SongDTO
import java.time.Instant

interface SongDAO {
    suspend fun allSongs(): List<SongDTO>
    suspend fun getSong(id: Int): SongDTO?
    suspend fun deleteSong(id: Int): Boolean
    suspend fun addNewSong(
        title: String, artist: String, duration: Int, plays: Int, lastPlayed: Instant?,
    ): SongDTO?

    suspend fun addNewSongIgnore(
        title: String, artist: String, duration: Int, plays: Int, lastPlayed: Instant?,
    ): SongDTO?

    suspend fun editSong(
        songId: Int, title: String, artist: String, duration: Int, plays: Int, lastPlayed: Instant?,
    ): Boolean

    suspend fun searchSong(title: String?, artist: String?): List<SongDTO>
    suspend fun deleteAllSongs(): Int
}