package br.com.peixinho_karaoke.models.dao

import br.com.peixinho_karaoke.models.Song

interface SongDAO {
    suspend fun allSongs(): List<Song>
    suspend fun getSong(id: Int): Song?
    suspend fun deleteSong(id: Int): Boolean
    suspend fun addNewSong(songId: Int, title: String, artist: String, duration: Int, combined: String): Song?
    suspend fun addNewSongIgnore(songId: Int, title: String, artist: String, duration: Int, combined: String): Song?
    suspend fun editSong(songId: Int, title: String, artist: String, duration: Int, combined: String): Boolean
    suspend fun searchSong(searchString: String): List<Song>
}