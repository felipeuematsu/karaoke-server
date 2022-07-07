package br.com.peixinho_karaoke.service

import br.com.peixinho_karaoke.configuration.ApplicationConfiguration
import br.com.peixinho_karaoke.models.Playlist
import br.com.peixinho_karaoke.models.State
import br.com.peixinho_karaoke.models.dao.RequestDAO
import br.com.peixinho_karaoke.models.dao.SongDAO
import br.com.peixinho_karaoke.models.dao.StateDAO
import br.com.peixinho_karaoke.models.dao.impl.RequestDAOImpl
import br.com.peixinho_karaoke.models.dao.impl.SongDAOImpl
import br.com.peixinho_karaoke.models.dao.impl.StateDAOImpl
import br.com.peixinho_karaoke.models.request.add_songs.AddSongsResponseDTO
import br.com.peixinho_karaoke.models.request.add_songs.NewSongDTO
import br.com.peixinho_karaoke.models.request.get_requests.GetRequestsResponseDTO
import br.com.peixinho_karaoke.models.response.GetSerialResponse
import br.com.peixinho_karaoke.models.response.GetVenuesResponse
import br.com.peixinho_karaoke.models.response.VenueResponse
import br.com.peixinho_karaoke.models.response.submit_request.SubmitRequestResponseDTO
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime

object ApiService {
    private val songDAO: SongDAO = SongDAOImpl()
    private val requestDAO: RequestDAO = RequestDAOImpl()
    private val stateDAO: StateDAO = StateDAOImpl()

    fun getSerial(): GetSerialResponse = try {
        GetSerialResponse(
            command = "getSerial",
            serial = runBlocking {
                stateDAO.getState(id = 0)?.serial ?: 0
            },
        )
    } catch (e: Exception) {
        GetSerialResponse(
            command = "getSerial",
            error = "true",
            serial = 0,
        )
    }

    fun getVenues(): GetVenuesResponse = try {
        GetVenuesResponse(venues = listOf(
            VenueResponse(
                venue_id = 0,
                name = ApplicationConfiguration.venueName,
                accepting = if (getAccepting()) 1 else 0,
                url_name = ApplicationConfiguration.venueUrl,
            ),
        ))
    } catch (e: Exception) {
        GetVenuesResponse(
            error = "true",
            venues = listOf(),
        )
    }

    private fun getAccepting(): Boolean = runBlocking {
        val dao: State? = stateDAO.getState(0)
        dao?.accepting ?: false
    }

    fun clearDatabase(): Map<String, String> = try {
        runBlocking {
            songDAO.deleteAllSongs()
            requestDAO.deleteAllRequests()
            mapOf("error" to "false", "command" to "clearDatabase")
        }
    } catch (e: Exception) {
        mapOf("error" to "true", "command" to "clearDatabase")
    }

    fun clearRequests(): Map<String, String> = try {
        runBlocking {
            requestDAO.allRequests().forEach { requestDAO.deleteRequest(it.requestId) }
            mapOf("error" to "false", "command" to "clearRequests")
        }
    } catch (ignored: Exception) {
        mapOf("error" to "true", "command" to "clearRequests")
    }

    fun deleteRequest(requestId: Int): Map<String, String> = try {
        runBlocking {
            requestDAO.getRequest(requestId)?.let {
                requestDAO.deleteRequest(it.requestId)
                songDAO.allSongs().filter { song -> song.title == it.title && song.artist == it.artist }
                    .forEach { song ->
                        songDAO.editSong(
                            songId = song.songId,
                            title = song.title,
                            artist = song.artist,
                            duration = song.duration,
                            plays = song.plays + 1,
                            combined = song.combined,
                            lastPlayed = song.lastPlayed,
                        )
                    }
            }

            mapOf("error" to "false", "command" to "deleteRequest")
        }
    } catch (ignored: Exception) {
        mapOf("error" to "true", "command" to "deleteRequest")
    }

    fun submitRequest(songId: Int, singerName: String, keyChange: Int?): SubmitRequestResponseDTO = try {
        runBlocking {
            val song = songDAO.getSong(songId)

            val newRequest = requestDAO.addNewRequest(
                title = song!!.title,
                artist = song.artist,
                singer = singerName,
                keyChange = keyChange ?: 0,
                requestTime = LocalDateTime.now(),
            )
            val requestId = newRequest?.requestId ?: throw Exception()
            return@runBlocking SubmitRequestResponseDTO(
                requestId = requestId,
                error = "false",
                command = "submitRequest",
            )
        }
    } catch (e: Exception) {
        println(e.message)
        SubmitRequestResponseDTO(error = "true", requestId = 0, command = "submitRequest")
    }

    fun search(searchString: String): Map<String, Any> = try {
        runBlocking { mapOf("error" to false, "songs" to songDAO.searchSong(searchString)) }
    } catch (e: Exception) {
        println(e.message)
        mapOf("error" to true, "songs" to listOf<br.com.peixinho_karaoke.models.SongDTO>())
    }


    fun connectionTest() = mapOf("connection" to "ok", "command" to "connectionTest")

    fun addSongs(songs: List<NewSongDTO>): AddSongsResponseDTO {
        println("Adding ${songs.size} songs")
        return runBlocking {
            val errors = mutableListOf<String>()
            var count = 0
            var lastArtist: String? = null
            var lastTitle: String? = null
            songs.forEach {
                try {
                    if (it.title == null || it.artist == null || it.duration == null || it.plays == null) {
                        errors.add("Title, artist, plays or duration is null")
                        return@forEach
                    }

                    lastArtist = it.artist
                    lastTitle = it.title
                    runBlocking {

                        val newSong = songDAO.addNewSongIgnore(
                            title = it.title,
                            artist = it.artist,
                            duration = it.duration,
                            plays = it.plays,
                            combined = it.artist + " " + it.title,
                            lastPlayed = it.lastPlayed,
                        )
                        if (newSong == null) {
                            errors.add("Song couldn't be added")
                        }
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
                last_title = lastTitle,
                last_artist = lastArtist,
                errors = errors,
                `entries processed` = count,
            )
        }
    }

    fun getRequests(): GetRequestsResponseDTO = runBlocking {
        try {
            GetRequestsResponseDTO(requests = requestDAO.allRequests(),
                error = "false",
                command = "getRequests",
                serial = stateDAO.getState(0)?.serial ?: 0)
        } catch (e: Exception) {
            GetRequestsResponseDTO(requests = listOf(),
                error = "true",
                command = "getRequests",
                serial = stateDAO.getState(0)?.serial ?: 0)
        }
    }

}
