package br.com.peixinho_karaoke.plugins

import br.com.peixinho_karaoke.configuration.ApplicationConfiguration
import br.com.peixinho_karaoke.models.request.ClientDefaultDTO
import br.com.peixinho_karaoke.models.request.add_songs.AddSongsRequestDTO
import br.com.peixinho_karaoke.models.request.delete_request.DeleteRequestDTO
import br.com.peixinho_karaoke.models.request.search.SearchDTO
import br.com.peixinho_karaoke.models.request.submit_request.SubmitRequestDTO
import br.com.peixinho_karaoke.service.ApiService
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun Application.configureRouting() {

    routing {
        post("/api") {
            val client = HttpClient(CIO) {
                install(ContentNegotiation) {
                    json(Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    })
                }

            }
            val request: ClientDefaultDTO = call.receive()

            if (request.apiKey != ApplicationConfiguration.apiKey) {
                return@post call.respond(HttpStatusCode.Unauthorized)
            }

            val response: HttpResponse = client.post {
                url {
                    host = "localhost"
                    port = ApplicationConfiguration.serverPort
                    path(request.command)
                }
                headers {
                    append("api_key", request.apiKey)
                }
                setBody(request)

                contentType(ContentType.Application.Json)
            }



            call.respondText(text = response.bodyAsText(), status = response.status)
            client.close()

        }
        post("/getSerial") {
            call.respond(message = ApiService.getSerial())
        }
        post("/getAlert") {
            call.respondText { "" }
        }
        post("/getVenues") {
            call.respond(message = ApiService.getVenues())
        }
        post("/getRequests") {
            call.respond(message = ApiService.getRequests())
        }
        post("/venueAccepting") {
            call.respond(message = ApiService.getVenues())
        }
        post("/clearDatabase") {
            call.respond(message = ApiService.clearDatabase())
        }
        post("/clearRequests") {
            call.respond(message = ApiService.clearRequests())
        }
        post("/deleteRequest") {
            val request: DeleteRequestDTO = call.receive()
            if (request.request_id == null) {
                return@post call.respond(HttpStatusCode.BadRequest)
            }
            call.respond(message = ApiService.deleteRequest(request.request_id))
        }
        post("/submitRequest") {
            val request: SubmitRequestDTO = call.receive()
            if (request.songId == null || request.singerName == null) {
                return@post call.respond(HttpStatusCode.BadRequest)
            }
            call.respond(message = ApiService.submitRequest(request.songId, request.singerName, request.keyChange))
        }
        post("/search") {
            val request: SearchDTO = call.receive()
            if (request.searchString == null) {
                return@post call.respond(HttpStatusCode.BadRequest)
            }
            call.respond(message = ApiService.search(request.searchString))
        }
        post("/connectionTest") {
            call.respond(message = ApiService.connectionTest())
        }
        post("/addSongs") {
            println("LOG addSongs")
            val request: AddSongsRequestDTO = call.receive()

            println(request)
            if (request.songs == null) {
                return@post call.respond(HttpStatusCode.BadRequest)
            }
            val message = ApiService.addSongs(request.songs)
            call.respond(message = message)
        }
    }
}
