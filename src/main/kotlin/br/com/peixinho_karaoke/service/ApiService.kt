package br.com.peixinho_karaoke.service

import br.com.peixinho_karaoke.configuration.ApplicationConfiguration
import br.com.peixinho_karaoke.models.State
import br.com.peixinho_karaoke.models.dao.impl.RequestDAOImpl
import br.com.peixinho_karaoke.models.dao.impl.SongDAOImpl
import br.com.peixinho_karaoke.models.dao.impl.StateDAOImpl
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

    fun clearDatabase() = try {
        runBlocking {
            StateDAOImpl().allStates().forEach { StateDAOImpl().deleteState(it.id) }
            SongDAOImpl().allSongs().forEach { SongDAOImpl().deleteSong(it.songId) }
            RequestDAOImpl().allRequests().forEach { RequestDAOImpl().deleteRequest(it.requestId) }
            mapOf("error" to "false", "command" to "clearDatabase")
        }
    } catch (e: Exception) {
        mapOf("error" to "true", "command" to "clearDatabase")
    }

    fun clearRequests() =
        try {
            runBlocking {
                RequestDAOImpl().allRequests().forEach { RequestDAOImpl().deleteRequest(it.requestId) }
                mapOf("error" to "false", "command" to "clearRequests")
            }
        } catch (ignored: Exception) {
            mapOf("error" to "true", "command" to "clearRequests")
        }

    fun deleteRequest(requestId: Int) =
        try {
            runBlocking {
                RequestDAOImpl().deleteRequest(requestId)
                mapOf("error" to "false", "command" to "deleteRequest")
            }
        } catch (ignored: Exception) {
            mapOf("error" to "true", "command" to "deleteRequest")
        }

    fun submitRequest(songId: Int, singerName: String): Any =
        try {
            runBlocking {
                val song = SongDAOImpl().getSong(songId)

                val newRequest = RequestDAOImpl().addNewRequest(
                    requestId = 0,
                    title = song!!.title,
                    artist = song.artist,
                    singer = singerName,
                    requestTime = LocalDateTime.now(),
                )
                mapOf("error" to "false", "command" to "submitRequest", "requestId" to newRequest!!.requestId)
            }
        } catch (e: Exception) {
            println(e.message)
            mapOf("error" to "true", "command" to "submitRequest")
        }

    fun search(searchString: String) {
        try {
            runBlocking {
                SongDAOImpl().searchSong(searchString)
            }
        } catch (e: Exception) {
            println(e.message)
        }
    }


}
