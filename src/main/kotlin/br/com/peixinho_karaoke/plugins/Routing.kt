package br.com.peixinho_karaoke.plugins

import br.com.peixinho_karaoke.models.request.ClientRequest
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
            val request = call.receive<ClientRequest>()
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
            call.respond(message = ApiService.deleteRequest())
        }
        post("/submitRequest") {
            call.respond(message = ApiService.submitRequest())
        }
    }
}
