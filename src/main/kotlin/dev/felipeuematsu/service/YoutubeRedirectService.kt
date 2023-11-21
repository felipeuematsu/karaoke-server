package dev.felipeuematsu.service

import dev.felipeuematsu.models.youtube.YoutubeManifest
import dev.felipeuematsu.models.youtube.YoutubeSearchDTO
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.reflect.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.util.logging.Logger

object YoutubeRedirectService {

    private val client = HttpClient(CIO) {

        install(HttpRedirect)
        install(HttpCache)
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }


    fun getManifest(id: String): YoutubeManifest = runBlocking {
        val response = client.get {
            url {
                protocol = URLProtocol.HTTP
                host = "localhost"
                port = 8080
                encodedPath = "/youtube/manifest"

            }
            parameter("id", id)
        }


        return@runBlocking response.body<YoutubeManifest>(typeInfo<YoutubeManifest>())
    }

    fun getSearch(query: String?, uuid: String?): YoutubeSearchDTO {
        // Request to http://localhost:8080/search using the parameters query and uuid
        // and return the response body as a YoutubeSearchDTO
        return runBlocking {
            val response = client.get {
                url {
                    protocol = URLProtocol.HTTP
                    host = "localhost"
                    port = 8080
                    encodedPath = "/youtube/search"

                }
                parameter("query", query)
                parameter("uuid", uuid)
            }

            return@runBlocking response.body<YoutubeSearchDTO>(typeInfo<YoutubeSearchDTO>())
        }
    }

}