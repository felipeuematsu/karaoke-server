package br.com.felipeuematsu.service

import br.com.felipeuematsu.entity.*
import io.ktor.http.HttpStatusCode
import io.ktor.websocket.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.minus
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.ZoneOffset

object QueueService {
    fun enterQueue(singer: Singer) {
        transaction {
            val newPosition =
                CurrentSingers
                    .selectAll()
                    .orderBy(CurrentSingers.position to SortOrder.DESC)
                    .limit(n = 1)
                    .firstOrNull()
                    ?.get(CurrentSingers.position) ?: 0
            CurrentSinger.new {
                singerId = singer.id
                name = singer.name
                position = newPosition
                addts = LocalDateTime.now().toInstant(ZoneOffset.UTC)
            }
        }
    }

    fun leaveQueue(singer: Singer) {
        transaction {
            val currentSinger = CurrentSinger.find { CurrentSingers.singer eq singer.id }.firstOrNull()
            val position = currentSinger?.position ?: 0
            currentSinger?.delete()
            CurrentSingers.update(
                where = { CurrentSingers.position greater position },
                body = { it[CurrentSingers.position] = CurrentSingers.position minus 1 }
            )
        }
    }

    fun getQueue(): List<QueueSongDTO> = transaction {
        QueueSong.all().toList().map(QueueSong::toDTO)
    }

    fun removeFromQueue(id: Int) {
        transaction {
            QueueSong.findById(id)?.delete()
        }
    }

    fun reorderSongToIndex(id: Int, newIndex: Int): Pair<String?, HttpStatusCode> =
        transaction {
            val song = QueueSong.findById(id) ?: return@transaction Pair("Song not found", HttpStatusCode.BadRequest)
            val currentPosition = song.position
            if (currentPosition == newIndex) return@transaction Pair("Song is already in position $newIndex",  HttpStatusCode.NotFound)
            if (currentPosition < newIndex) {
                QueueSongs.update(
                    where = { QueueSongs.position greater currentPosition and (QueueSongs.position lessEq newIndex) },
                    body = { it[position] = position minus 1 }
                )
            } else {
                QueueSongs.update(
                    where = { QueueSongs.position less currentPosition and (QueueSongs.position greaterEq newIndex) },
                    body = { it[position] = position plus 1 }
                )
            }
            song.position = newIndex
            Pair(null, HttpStatusCode.OK)
        }

    fun skip(session: DefaultWebSocketSession) {
        try {
            suspend { session.send(Frame.Text("skip")) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getNextSong(): QueueSongDTO? = transaction {
        val first = QueueSong.find { QueueSongs.position eq 0 }.firstOrNull() ?: return@transaction null
        val song = Song.findById(first.song) ?: return@transaction null

        song.plays++

        first.delete()
        updateQueue()
        return@transaction first.toDTO()
    }

    private fun addToQueue(songDTO: Song, singerDTO: Singer, change: Int = 0): Int =
        transaction {
            QueueSong.new {
                song = songDTO.id.value
                singer = singerDTO.id.value
                position = (QueueSong
                    .all()
                    .orderBy(QueueSongs.position to SortOrder.DESC)
                    .limit(n = 1)
                    .firstOrNull()?.position ?: 0) + 1

                keyChange = change
            }.position
        }

    fun clearQueue() = transaction {
        QueueSong.all().forEach {
            it.delete()
        }
    }

    fun addSongToQueueRequest(songId: Int, singer: String): Int =
        transaction {
            val singerDTO = Singer.find { Singers.name eq singer }.firstOrNull() ?: return@transaction -1
            val songDTO = Song.find { DBSongs.id eq songId }.firstOrNull() ?: return@transaction -1
            return@transaction addToQueue(songDTO, singerDTO)
        }

    fun updateQueue() = transaction {
        if (QueueSong.all().empty()) return@transaction
        val lowest = QueueSong.all().orderBy(QueueSongs.position to SortOrder.ASC).first()
        if (lowest.position != 0) {
            QueueSongs.update {
                it[position] = position - lowest.position
            }
        }
    }
}

