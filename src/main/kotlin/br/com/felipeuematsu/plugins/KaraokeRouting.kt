package br.com.felipeuematsu.plugins

import br.com.felipeuematsu.entity.QueueSongDTO
import br.com.felipeuematsu.entity.SingerDTO
import br.com.felipeuematsu.models.request.add_path.AddPathRequestDTO
import br.com.felipeuematsu.models.request.add_songs.AddSongsRequestDTO
import br.com.felipeuematsu.models.request.submit_request.SubmitRequestDTO
import br.com.felipeuematsu.service.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import io.ktor.websocket.serialization.*
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.Json
import java.nio.charset.Charset

fun Application.configureRouting() {

    routing {
        var webSocketSession: DefaultWebSocketSession? = null
        webSocket {
            webSocketSession = this
//            while (isActive) {
//                val message = incoming.receive()
//                println(message)
//            }
            println("Session started successfully")
        }
        delete("/clearDatabase") {
            call.respond(message = ApiService.clearDatabase())
        }
        get("/search") {
            val title = call.request.queryParameters["title"]
            val artist = call.request.queryParameters["artist"]
            if (title != null || artist != null) {
                call.respond(message = ApiService.search(title, artist))
            } else {
                call.respond(HttpStatusCode.BadRequest)
            }
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
                return@post call.respond(HttpStatusCode.BadRequest, "Path or regex must be informed")
            }
            ApiService.addFolderRepository(request.path, request.regex, request.titlePos, request.artistPos)?.let {
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

        get("/images/{search}") {
            val search = call.parameters["search"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val image = SpotifyService.searchImages(search) ?: return@get call.respond(HttpStatusCode.NotFound)

            call.respondText { image }
        }

        post("/playlist/update-default") {
            call.respond(PlaylistsService.updateDefaultPlaylists())
        }

        get("/queue") {
            call.respond(QueueService.getQueue())
        }

        delete("/queue") {
            val dto = call.receive<QueueSongDTO>()
            if (dto.song == null || dto.singer == null) {
                return@delete call.respond(HttpStatusCode.BadRequest, "Song and Singer must not be null")
            }
            call.respond(QueueService.removeFromQueue(dto.song, dto.singer))
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
            val idString = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Id must not be null")
            val id = idString.toIntOrNull() ?: return@post call.respond(HttpStatusCode.BadRequest, "Id must be a number")
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
    }
}
