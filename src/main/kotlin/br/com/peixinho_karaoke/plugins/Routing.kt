package br.com.peixinho_karaoke.plugins

import br.com.peixinho_karaoke.configuration.ApplicationConfiguration
import br.com.peixinho_karaoke.models.request.ClientDefaultDTO
import br.com.peixinho_karaoke.models.request.delete_request.DeleteRequestDTO
import br.com.peixinho_karaoke.models.request.search.SearchDTO
import br.com.peixinho_karaoke.models.request.submit_request.SubmitRequestDTO
import br.com.peixinho_karaoke.service.ApiService
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        post("/api") {
            val client = HttpClient(CIO)
            val request: ClientDefaultDTO = call.receive()

            if (request.apiKey != ApplicationConfiguration.apiKey) {
                return@post call.respond(HttpStatusCode.Unauthorized)
            }

            client.use {
                val response: HttpResponse = client.post {
                    url {
                        host = "localhost"
                        port = 8080
                        path(request.command)
                    }
                    headers {
                        append("api_key", request.apiKey)
                    }
                }

                call.respondText(status = response.status) {
                    response.body()
                }
                client.close()
            }
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
            val request: ClientDefaultDTO = call.receive()

            if (request.songs == null) {
                return@post call.respond(HttpStatusCode.BadRequest)
            }
            call.respond(message = ApiService.addSongs(request.songs))
        }
    }
}
