package br.com.felipeuematsu.service

import br.com.felipeuematsu.entity.ArtistImage
import br.com.felipeuematsu.entity.ArtistImages
import br.com.felipeuematsu.entity.TrackImage
import br.com.felipeuematsu.entity.TrackImages
import br.com.felipeuematsu.models.spotify.SearchResponseDTO
import br.com.felipeuematsu.models.spotify.TokenResponseDTO
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.and
import java.time.LocalDateTime

object SpotifyService {
    private var accessToken: String? = null
    private var accessTokenExpiration: LocalDateTime? = null

    private const val clientId = "dbf8873ee2cb48d9a766122b936b7b1a"
    private const val clientSecret = "6335621267c0498aa12fad224880ae22"
    private const val baseUrl = "https://api.spotify.com/v1"
    private const val tokenUrl = "https://accounts.spotify.com/api/token"
    private val client = HttpClient(CIO) {
        install(HttpCache)
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
                    FormDataContent(Parameters.build {
                        append("grant_type", "client_credentials")
                    })
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

    fun searchSongImage(song: String, artist: String): String? {
        TrackImage.find { (TrackImages.title like song) and (TrackImages.artist like artist) }.firstOrNull()?.let {
            return it.url
        }
        val filteredSong = song.split('[').first().trim()
        val response = search("track:$filteredSong artist:$artist", "track", 1)
        val url = response.tracks?.items?.firstOrNull()?.album?.images?.firstOrNull()?.url ?: searchArtistImages(artist)
        url?.let {
            TrackImage.new {
                this.title = song
                this.artist = artist
                this.url = url
            }
        }
        return url
    }

    fun searchArtistImages(searchParam: String): String? {
        ArtistImage.find { ArtistImages.name like "%$searchParam%" }.firstOrNull()?.let {
            return it.url
        }
        val response = search(searchParam, "artist", 1)
        val url = response.artists?.items?.firstOrNull()?.images?.firstOrNull()?.url
        url?.let {
            ArtistImage.new {
                name = searchParam
                this.url = url
            }
        }
        return url
    }
}