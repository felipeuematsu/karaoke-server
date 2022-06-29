package br.com.peixinho_karaoke.models.dao

import br.com.peixinho_karaoke.models.Song

interface SongDAO {
    suspend fun allSongs(): List<Song>
    suspend fun getSong(id: Int): Song?
    suspend fun deleteSong(id: Int): Boolean
    suspend fun addNewSong(songId: Int, title: String, artist: String, combined: String): Song?
    suspend fun addNewSongIgnore(songId: Int, title: String, artist: String, combined: String): Song?
    suspend fun editSong(songId: Int, title: String, artist: String, combined: String): Boolean
}