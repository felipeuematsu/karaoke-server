package br.com.felipeuematsu.service

import br.com.felipeuematsu.entity.DBSongs
import br.com.felipeuematsu.entity.Repositories
import br.com.felipeuematsu.entity.Repository
import br.com.felipeuematsu.entity.RepositoryDTO
import br.com.felipeuematsu.entity.Song
import br.com.felipeuematsu.entity.SongDTO
import br.com.felipeuematsu.entity.SongResponseDTO
import br.com.felipeuematsu.models.YoutubeSongDTO
import br.com.felipeuematsu.models.dao.SongDAO
import br.com.felipeuematsu.models.dao.impl.SongDAOImpl
import br.com.felipeuematsu.models.request.add_songs.AddSongsResponseDTO
import br.com.felipeuematsu.models.request.add_songs.NewSongDTO
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.InternalAPI
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.jaudiotagger.audio.AudioFileIO
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.io.File
import java.util.zip.ZipFile

object ApiService {
    private val logger = LoggerFactory.getLogger(ApiService::class.java)
    private val songDAO: SongDAO = SongDAOImpl()

    fun clearDatabase(): Map<String, String> = try {
        runBlocking {
            songDAO.deleteAllSongs()
            mapOf("error" to "false", "command" to "clearDatabase")
        }
    } catch (e: Exception) {
        mapOf("error" to "true", "command" to "clearDatabase")
    }

    fun search(title: String?, artist: String?, page: Int, pageCount: Int): SongResponseDTO =
        runBlocking {
            songDAO.searchSong(title, artist, page, pageCount)
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

    fun addFolderRepository(path: String, regex: String, titlePos: Int, artistPost: Int): String? = try {
        transaction {
            if (!File(path).exists() || !File(path).isDirectory) {
                return@transaction "Folder doesn't exist"
            }
            val repository = Repository.find { Repositories.path eq path }.firstOrNull()
            if (repository == null) {
                Repository.new {
                    this.path = path
                    this.regex = regex
                    this.titlePos = titlePos
                    this.artistPos = artistPost
                }
                null
            } else {
                return@transaction "Repository already exists"
            }
        }
    } catch (e: Exception) {
        e.message
    }

    private fun getDownloadRepository(): Repository? = transaction {
        Repository.find { Repositories.path.lowerCase() like "%download%" }.firstOrNull()
    }

    fun getFolderRepositories(): List<RepositoryDTO> = transaction {
        Repository.find { Repositories.path.isNotNull() }.map(Repository::toDTO).toList()
    }

    fun getSong(id: Int): SongDTO? =
        runBlocking { songDAO.getSong(id) }

    fun updateFolderRepositories() {
        transaction {
            DBSongs.deleteAll()
            val repositories = getFolderRepositories()
            repositories.forEach { repository ->
                val regex = Regex(repository.regex)
                val files = File(repository.path).listFiles() ?: arrayOf<File>()
                files.toList().forEachParallel {
                    if (it.isFile) {
                        var mp3Duration = 0
                        if (it.name.endsWith(".zip")) {
                            val match = regex.matchEntire(it.name.split(".").first())
                            withContext(Dispatchers.IO) {
                                ZipFile(it).use { zip ->
                                    zip.entries().asSequence().forEach { entry ->
                                        if (entry.name.endsWith("mp3")) {
                                            val tempFile = File.createTempFile("temp_${entry.name}", ".mp3")
                                            tempFile.deleteOnExit()
                                            zip.getInputStream(entry).use { tempFile.writeBytes(it.readBytes()) }
                                            mp3Duration = AudioFileIO.read(tempFile).audioHeader.trackLength
                                        }
                                    }
                                }
                            }
                            logger.info("Adding ${it.name} to database")
                            Song.new {
                                title = match?.groups?.get(repository.titlePos)?.value ?: ""
                                artist = match?.groups?.get(repository.artistPos)?.value ?: ""
                                duration = mp3Duration
                                plays = 0
                                filename = it.name
                                path = it.absolutePath
                                searchString = "${it.name} ${it.absolutePath}"
                            }
                        } else if (
                            it.name.endsWith(".mp3") &&
                            File(it.absolutePath.replace(".mp3", ".cdg")).exists()
                        ) {
                            val match = regex.matchEntire(it.name.split(".").first())
                            mp3Duration = AudioFileIO.read(File(it.absolutePath)).audioHeader.trackLength
                            logger.info("Adding ${it.name} to database")
                            Song.new {
                                title = match?.groups?.get(repository.titlePos)?.value ?: ""
                                artist = match?.groups?.get(repository.artistPos)?.value ?: ""
                                duration = mp3Duration
                                plays = 0
                                filename = it.name
                                path = it.absolutePath
                                searchString = "${it.name} ${it.absolutePath}"
                            }
                        }
                    }
                }

            }
        }
    }

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

    @OptIn(InternalAPI::class)
    suspend fun addYoutubeSong(dto: YoutubeSongDTO): String? {
        val repo = getDownloadRepository() ?: return "No download repository set"

        val url = dto.url ?: return "No url set"

        if (dto.title == null || dto.artist == null || dto.duration == null) {
            return "Title, artist or duration is null"
        }
        val res = client.get(url) {
            method = HttpMethod.Get
        }

        val file = File("${repo.path}/${dto.artist.replace("/", " ")} - ${dto.title.replace("/", " ")}.mp4")

        println(file.absolutePath)
        if (!file.exists()) {
            withContext(Dispatchers.IO) {
                file.createNewFile()
            }
        }
        res.call.response.content.copyAndClose(file.writeChannel())

        transaction {
            Song.new {
                title = dto.title
                artist = dto.artist
                duration = dto.duration
                plays = 0
                filename = "${dto.artist} - ${dto.title}.mp4"
                path = file.absolutePath
                searchString = "${dto.artist} - ${dto.title}.mp4 ${file.absolutePath}"
            }
        }
        return null
    }
}


fun <A> Collection<A>.forEachParallel(f: suspend (A) -> Unit): Unit = runBlocking {
    map { async { f(it) } }.forEach { it.await() }
}