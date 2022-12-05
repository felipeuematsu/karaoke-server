package br.com.felipeuematsu.plugins

import br.com.felipeuematsu.entity.SingerDTO
import br.com.felipeuematsu.models.YoutubeSongDTO
import br.com.felipeuematsu.models.request.QueueReorderRequestDTO
import br.com.felipeuematsu.models.request.add_path.AddPathRequestDTO
import br.com.felipeuematsu.models.request.add_songs.AddSongsRequestDTO
import br.com.felipeuematsu.models.request.submit_request.SubmitRequestDTO
import br.com.felipeuematsu.models.response.CurrentSongDTO
import br.com.felipeuematsu.service.ApiService
import br.com.felipeuematsu.service.PlaylistsService
import br.com.felipeuematsu.service.QueueService
import br.com.felipeuematsu.service.SingerService
import br.com.felipeuematsu.service.SpotifyService
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.http.content.react
import io.ktor.server.http.content.singlePageApplication
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import io.ktor.server.websocket.receiveDeserialized
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.serialization.sendSerializedBase
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.charset.Charset

fun Application.configureRouting() {
    var currentSong: CurrentSongDTO? = null

    routing {
        get("/health") {
            call.respondText("OK", status = HttpStatusCode.OK)
        }

        get("/.well-known/assetlinks.json") {
            call.respondText(
                File("resources/assetlinks.json").readText(Charset.defaultCharset()),
                contentType = io.ktor.http.ContentType.Application.Json
            )
        }

        singlePageApplication {
            react("flutter_resources")
        }

        var webSocketSession: DefaultWebSocketSession? = null
        webSocket {
            webSocketSession = this

            while (isActive) {
                try {
                    currentSong = receiveDeserialized<CurrentSongDTO>()
                } catch (e: Exception) {
                    println("Error receiving current song: ${e.message}")
                }
            }
            println("Session started successfully")
        }
        delete("/clearDatabase") {
            call.respond(message = ApiService.clearDatabase())
        }
        get("/search") {
            val title = call.request.queryParameters["title"]
            val artist = call.request.queryParameters["artist"]
            val page = call.request.queryParameters["page"]?.toInt() ?: 1
            val pageCount = call.request.queryParameters["pageCount"]?.toInt() ?: 10
            if (title == null && artist == null) {
                return@get call.respond(HttpStatusCode.BadRequest)
            }
            val response = ApiService.search(title, artist, page, pageCount)
            call.respond(message = response)

        }
        post("/connectionTest") {
            call.respond(message = ApiService.connectionTest())
        }
        post("/addSongs") {
            val request: AddSongsRequestDTO = call.receive()
            if (request.songs == null) {
                return@post call.respond(HttpStatusCode.BadRequest)
            }
            val message = ApiService.addSongs(request.songs)
            call.respond(message = message)
        }

        post("/path") {
            val request: AddPathRequestDTO = call.receive()
            if (request.path == null || request.regex == null) {
                return@post call.respond(HttpStatusCode.BadRequest, "Path and regex must be informed")
            }
            ApiService.addFolderRepository(request.path, request.regex, request.titlePos, request.artistPos)?.let {
                call.respond(status = HttpStatusCode.BadRequest, message = it)
            } ?: call.respond(HttpStatusCode.OK)

        }

        put("/path") {
            val repos: List<AddPathRequestDTO> = call.receive()
            if (repos.any { repo -> repo.path == null || repo.regex == null }) {
                return@put call.respond(HttpStatusCode.BadRequest, "Path and regex must be informed")
            }
            repos.forEach { repo ->
                val error =
                    ApiService.addFolderRepository(repo.path!!, repo.regex!!, repo.titlePos, repo.artistPos)
                if (error != null) {
                    return@put call.respond(status = HttpStatusCode.BadRequest, message = error)
                }
            }

            call.respond(HttpStatusCode.OK)

        }

        post("/path/download") {
            val path: String = call.receive()

            ApiService.addFolderRepository("$path/download", "(.*) - (.*)", 1, 0)?.let {
                call.respond(status = HttpStatusCode.BadRequest, message = it)
            } ?: call.respond(HttpStatusCode.OK)

        }

        post("/path/update") {
            call.respond(ApiService.updateFolderRepositories())
        }

        get("/path") {
            call.respond(ApiService.getFolderRepositories())
        }

        get("/playlist") {
            call.respond(PlaylistsService.getPlaylists())
        }

        get("/playlist/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
            val playlist = PlaylistsService.getPlaylist(id) ?: return@get call.respond(HttpStatusCode.NotFound)
            call.respond(playlist)
        }

        get("/images/{search}") {
            val search = call.parameters["search"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val image = SpotifyService.searchArtistImages(search) ?: return@get call.respond(HttpStatusCode.NotFound)

            call.respondText { image }
        }

        post("/playlist/update-default") {
            call.respond(PlaylistsService.updateDefaultPlaylists())
        }

        get("/queue") {
            call.respond(QueueService.getQueue())
        }

        delete("/queue/{queueSongId}") {
            val queueSongId = call.parameters["queueSongId"]?.toIntOrNull() ?: return@delete call.respond(
                HttpStatusCode.BadRequest,
                "Invalid id"
            )
            call.respond(QueueService.removeFromQueue(queueSongId))
        }

        put("/queue/reorder") {
            val request: QueueReorderRequestDTO = call.receive()
            if (request.queueSongId == null || request.newIndex == null) {
                return@put call.respond(HttpStatusCode.BadRequest, "Request must have a queueSongId and newIndex")
            }
            val response = QueueService.reorderSongToIndex(request.queueSongId, request.newIndex)
            val string = response.first
            return@put if (string != null) {
                call.respond(status = response.second, message = string)
            } else {
                call.respond(response.second)
            }
        }

        post("/queue/skip") {
            val session = webSocketSession ?: return@post call.respond(
                HttpStatusCode.FailedDependency,
                "No WebSocket session found. Try opening the player again."
            )
            session.send(Frame.Text("skip"))

            call.respond(HttpStatusCode.NoContent)
        }

        get("/queue/next") {
            val nextSong = QueueService.getNextSong() ?: return@get call.respond(HttpStatusCode.NotFound)

            call.respond(nextSong)
        }

        post("play") {
            val session = webSocketSession ?: return@post call.respond(
                HttpStatusCode.FailedDependency,
                "No WebSocket session found. Try opening the player again."
            )
            session.send(Frame.Text("play"))
            call.respond(HttpStatusCode.NoContent)
        }

        post("pause") {
            val session = webSocketSession ?: return@post call.respond(
                HttpStatusCode.FailedDependency,
                "No WebSocket session found. Try opening the player again."
            )
            session.send(Frame.Text("pause"))
            call.respond(HttpStatusCode.NoContent)
        }

        post("stop") {
            val session = webSocketSession ?: return@post call.respond(
                HttpStatusCode.FailedDependency,
                "No WebSocket session found. Try opening the player again."
            )
            session.send(Frame.Text("stop"))
            call.respond(HttpStatusCode.NoContent)
        }

        post("/queue/clear") {
            QueueService.clearQueue()
            call.respond(HttpStatusCode.NoContent)
        }

        post("/restart") {
            val session = webSocketSession ?: return@post call.respond(
                HttpStatusCode.FailedDependency,
                "No WebSocket session found. Try opening the player again."
            )
            session.send(Frame.Text("restart"))
            call.respond(HttpStatusCode.NoContent)
        }

        post("/play/{id}") {
            val idString =
                call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Id must not be null")
            val id =
                idString.toIntOrNull() ?: return@post call.respond(HttpStatusCode.BadRequest, "Id must be a number")
            val session = webSocketSession ?: return@post call.respond(
                HttpStatusCode.FailedDependency,
                "No WebSocket session found. Try opening the player again."
            )
            val songDTO = ApiService.getSong(id) ?: return@post call.respond(HttpStatusCode.NotFound)
            session.sendSerializedBase(songDTO, KotlinxWebsocketSerializationConverter(Json), Charset.defaultCharset())
            call.respond(HttpStatusCode.NoContent)
        }

        post("/volume/up") {
            val session = webSocketSession ?: return@post call.respond(
                HttpStatusCode.FailedDependency,
                "No WebSocket session found. Try opening the player again."
            )
            session.send(Frame.Text("volumeUp"))
            call.respond(HttpStatusCode.NoContent)
        }

        post("/volume/down") {
            val session = webSocketSession ?: return@post call.respond(
                HttpStatusCode.FailedDependency,
                "No WebSocket session found. Try opening the player again."
            )
            session.send(Frame.Text("volumeDown"))
            call.respond(HttpStatusCode.NoContent)
        }

        post("/queue") {
            val submitRequestDTO = call.receive<SubmitRequestDTO>()

            if (submitRequestDTO.songId == null) {
                return@post call.respond(HttpStatusCode.BadRequest, "Song Id must not be null")
            }
            if (submitRequestDTO.singerName == null) {
                return@post call.respond(HttpStatusCode.BadRequest, "Singer must not be null")
            }
            val position = QueueService.addSongToQueueRequest(submitRequestDTO.songId, submitRequestDTO.singerName)
            call.respond(position)
            QueueService.updateQueue()
        }

        post("/singer") {
            val singerDTO = call.receive<SingerDTO>()
            call.respond(SingerService.addSinger(singerDTO.name))
        }

        get("/playing") {
            val songDTO =
                currentSong?.songId?.let { ApiService.getSong(it) } ?: return@get call.respond(HttpStatusCode.NoContent)
            currentSong = currentSong?.copy(song = songDTO)
            val playingDTO = currentSong ?: return@get call.respond(HttpStatusCode.NoContent)
            call.respond(playingDTO)
        }

        get("/singers") {
            call.respond(SingerService.getSingers())
        }

        put("/singer") {
            val singerDTO = call.receive<SingerDTO>()
            val result = SingerService.updateSinger(singerDTO) ?: return@put call.respond(HttpStatusCode.NotFound)
            call.respond(result)
        }

        post("/song/youtube") {
            val youtubeSongDTO = call.receive<YoutubeSongDTO>()
            val songDTO = ApiService.addYoutubeSong(youtubeSongDTO) ?: return@post call.respond(HttpStatusCode.OK)
            call.respond(HttpStatusCode.BadRequest, songDTO)
        }

        post("/skip") {
            val session = webSocketSession ?: return@post call.respond(
                HttpStatusCode.FailedDependency,
                "No WebSocket session found. Try opening the player again."
            )
            session.send(Frame.Text("skip"))
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
