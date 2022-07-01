package br.com.peixinho_karaoke.service

import br.com.peixinho_karaoke.configuration.ApplicationConfiguration
import br.com.peixinho_karaoke.models.Song
import br.com.peixinho_karaoke.models.State
import br.com.peixinho_karaoke.models.dao.impl.RequestDAOImpl
import br.com.peixinho_karaoke.models.dao.impl.SongDAOImpl
import br.com.peixinho_karaoke.models.dao.impl.StateDAOImpl
import br.com.peixinho_karaoke.models.request.add_songs.AddSongsResponseDTO
import br.com.peixinho_karaoke.models.request.add_songs.SongDTO
import br.com.peixinho_karaoke.models.request.get_requests.GetRequestsResponseDTO
import br.com.peixinho_karaoke.models.response.GetSerialResponse
import br.com.peixinho_karaoke.models.response.GetVenuesResponse
import br.com.peixinho_karaoke.models.response.VenueResponse
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime

object ApiService {
    fun getSerial(): GetSerialResponse =
        try {
            GetSerialResponse(
                command = "getSerial",
                serial = runBlocking {
                    val dao: State? = StateDAOImpl().getState(0)
                    dao?.serial ?: 0
                },
            )
        } catch (e: Exception) {
            GetSerialResponse(
                command = "getSerial",
                error = "true",
                serial = 0,
            )
        }

    fun getVenues(): GetVenuesResponse =
        try {
            GetVenuesResponse(
                com = "getVenues",
                venues = listOf(
                    VenueResponse(
                        venue_id = 0,
                        name = "Venue 1",
                        accepting = if (getAccepting()) 1 else 0,
                        url_name = ApplicationConfiguration.venueName,
                    ),
                )
            )
        } catch (e: Exception) {
            GetVenuesResponse(
                com = "getVenues",
                err = "true",
                venues = listOf(),
            )
        }


    private fun getAccepting(): Boolean =
        runBlocking {
            val dao: State? = StateDAOImpl().getState(0)
            dao?.accepting ?: false
        }

    fun clearDatabase(): Map<String, String> = try {
        runBlocking {
            StateDAOImpl().allStates().forEach { StateDAOImpl().deleteState(it.id) }
            SongDAOImpl().allSongs().forEach { SongDAOImpl().deleteSong(it.songId) }
            RequestDAOImpl().allRequests().forEach { RequestDAOImpl().deleteRequest(it.requestId) }
            mapOf("error" to "false", "command" to "clearDatabase")
        }
    } catch (e: Exception) {
        mapOf("error" to "true", "command" to "clearDatabase")
    }

    fun clearRequests(): Map<String, String> =
        try {
            runBlocking {
                RequestDAOImpl().allRequests().forEach { RequestDAOImpl().deleteRequest(it.requestId) }
                mapOf("error" to "false", "command" to "clearRequests")
            }
        } catch (ignored: Exception) {
            mapOf("error" to "true", "command" to "clearRequests")
        }

    fun deleteRequest(requestId: Int): Map<String, String> =
        try {
            runBlocking {
                RequestDAOImpl().deleteRequest(requestId)
                mapOf("error" to "false", "command" to "deleteRequest")
            }
        } catch (ignored: Exception) {
            mapOf("error" to "true", "command" to "deleteRequest")
        }

    fun submitRequest(songId: Int, singerName: String, keyChange: Int?): Map<String, Any> =
        try {
            runBlocking {
                val song = SongDAOImpl().getSong(songId)

                val newRequest = RequestDAOImpl().addNewRequest(
                    title = song!!.title,
                    artist = song.artist,
                    singer = singerName,
                    keyChange = keyChange ?: 0,
                    requestTime = LocalDateTime.now(),
                )
                mapOf("error" to "false", "command" to "submitRequest", "requestId" to newRequest!!.requestId)
            }
        } catch (e: Exception) {
            println(e.message)
            mapOf("error" to "true", "command" to "submitRequest")
        }

    fun search(searchString: String): Map<String, Any> =
        try {
            runBlocking {
                return@runBlocking mapOf("error" to false, "songs" to SongDAOImpl().searchSong(searchString))
            }
        } catch (e: Exception) {
            println(e.message)
            mapOf("error" to true, "songs" to listOf<Song>())
        }


    fun connectionTest() =
        mapOf("connection" to "ok", "command" to "connectionTest")

    fun addSongs(songs: List<SongDTO>): AddSongsResponseDTO {
        println("Adding ${songs.size} songs")
        return runBlocking {
            val errors = mutableListOf<String>()
            var count = 0
            var artist: String? = null
            var title: String? = null
            songs.forEach {
                try {
                    if (it.title == null || it.artist == null || it.duration == null) {
                        errors.add("Title, artist or duration is null")
                        return@forEach
                    }

                    artist = it.artist
                    title = it.title
                    val newSong = SongDAOImpl().addNewSongIgnore(
                        title = it.title,
                        artist = it.artist,
                        combined = it.artist + " " + it.title,
                        duration = it.duration,
                    )
                    if (newSong == null) {
                        errors.add("Song couldn't be added")
                    }
                    count++
                } catch (e: Exception) {
                    errors.add(e.message ?: "Unknown error")
                    println(e.message)
                    count++
                }
            }
            AddSongsResponseDTO(
                command = "addSongs",
                error = if (errors.isEmpty()) "false" else "true",
                last_title = title,
                last_artist = artist,
                errors = errors,
                `entries processed` = count,
            )
        }
    }

    fun getRequests(): GetRequestsResponseDTO =
        runBlocking {
            try {
                GetRequestsResponseDTO(
                    requests = RequestDAOImpl().allRequests(),
                    error = "false",
                    command = "getRequests",
                    serial = StateDAOImpl().getState(0)?.serial ?: 0
                )
            } catch (e: Exception) {
                GetRequestsResponseDTO(
                    requests = listOf(),
                    error = "true",
                    command = "getRequests",
                    serial = StateDAOImpl().getState(0)?.serial ?: 0
                )
            }
        }

}
