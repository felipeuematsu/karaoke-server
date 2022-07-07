package br.com.peixinho_karaoke.service

import br.com.peixinho_karaoke.models.spotify.SearchResponseDTO
import br.com.peixinho_karaoke.models.spotify.TokenResponseDTO
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.time.LocalDateTime

object SpotifyService {
    private var accessToken: String? = null
    private var accessTokenExpiration: LocalDateTime? = null

    private const val clientId = "dbf8873ee2cb48d9a766122b936b7b1a"
    private const val clientSecret = "6335621267c0498aa12fad224880ae22"
    private const val baseUrl = "https://api.spotify.com/v1"
    private const val tokenUrl = "https://accounts.spotify.com/api/token"
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    private fun getToken() {
        runBlocking {
            val response = client.submitForm(tokenUrl) {
                method = HttpMethod.Post
                basicAuth(clientId, clientSecret)
                setBody(
                    FormDataContent(formData =
                    Parameters.build {
                        append("grant_type", "client_credentials")
                    }
                    )
                )
            }

            val dto: TokenResponseDTO = response.body()
            accessToken = dto.access_token
            dto.expires_in?.let { accessTokenExpiration = LocalDateTime.now().plusSeconds(it) }
        }

    }


    private fun search(searchParam: String, type: String = "artist", limit: Int = 20): SearchResponseDTO {
        if (accessToken == null || accessTokenExpiration == null || accessTokenExpiration?.isBefore(LocalDateTime.now()) == true) {
            getToken()
        }
        return runBlocking {
            val response = client.submitForm("$baseUrl/search") {
                method = HttpMethod.Get
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                parameter("q", searchParam)
                parameter("type", type)
                parameter("limit", limit)

            }
            return@runBlocking response.body<SearchResponseDTO>()
        }
    }

    fun searchImages(searchParam: String): String? {
        val response = search(searchParam, "artist",1)
        return response.artists?.items?.firstOrNull()?.images?.firstOrNull()?.url
    }
}