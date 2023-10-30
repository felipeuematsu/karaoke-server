package dev.felipeuematsu.database

import dev.felipeuematsu.entity.*
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.*
import org.jetbrains.exposed.sql.transactions.experimental.*

object KaraokeDatabase {
    fun init() {
        val driverClassName = "org.sqlite.JDBC"
        val currentDir = System.getProperty("user.dir")
        val jdbcURL = "jdbc:sqlite:file:$currentDir/karaoke.sqlite"
        val database = Database.connect(jdbcURL, driverClassName)
        transaction(database) {
            SchemaUtils.create(DBSongs)
            SchemaUtils.create(Playlists)
            SchemaUtils.create(PlaylistSongs)
            SchemaUtils.create(Singers)
            SchemaUtils.create(QueueSongs)
            SchemaUtils.create(CurrentSingers)
            SchemaUtils.create(Requests)
            SchemaUtils.create(Repositories)
            SchemaUtils.create(ArtistImages)
            SchemaUtils.create(TrackImages)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}