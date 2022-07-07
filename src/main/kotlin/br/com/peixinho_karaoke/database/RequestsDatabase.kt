package br.com.peixinho_karaoke.database

import br.com.peixinho_karaoke.models.*
import br.com.peixinho_karaoke.models.dao.impl.StateDAOImpl
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.*
import org.jetbrains.exposed.sql.transactions.experimental.*

object RequestsDatabaseFactory {
    fun init() {
        val driverClassName = "org.sqlite.JDBC"
        val jdbcURL = "jdbc:sqlite:file:./build/req_db"
        val database = Database.connect(jdbcURL, driverClassName)
        transaction(database) {
            SchemaUtils.create(Songs)
            SchemaUtils.create(Requests)
            SchemaUtils.create(States)

            SchemaUtils.create(Playlists)
            SchemaUtils.create(PlaylistSongs)
            runBlocking {
                StateDAOImpl().addOrUpdateNewState(0, false, 1)
            }


        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}