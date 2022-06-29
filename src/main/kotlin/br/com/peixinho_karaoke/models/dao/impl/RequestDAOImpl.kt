package br.com.peixinho_karaoke.models.dao.impl

import br.com.peixinho_karaoke.database.RequestsDatabaseFactory.dbQuery
import br.com.peixinho_karaoke.models.Request
import br.com.peixinho_karaoke.models.dao.RequestDAO
import br.com.peixinho_karaoke.models.Requests
import org.jetbrains.exposed.sql.*
import java.time.LocalDateTime

class RequestDAOImpl : RequestDAO {
    private fun resultRowToRequest(row: ResultRow) = Request(
        requestId = row[Requests.requestId],
        title = row[Requests.title],
        artist = row[Requests.artist],
        singer = row[Requests.singer],
        requestTime = row[Requests.requestTime]
    )

    override suspend fun allRequests(): List<Request> = dbQuery {
        Requests.selectAll().map(::resultRowToRequest).toList()
    }

    override suspend fun getRequest(id: Int): Request? = dbQuery {
        Requests.select { Requests.requestId eq id }
            .map(::resultRowToRequest)
            .singleOrNull()
    }

    override suspend fun addNewRequest(
        requestId: Int,
        title: String,
        artist: String,
        singer: String,
        requestTime: LocalDateTime
    ): Request? = dbQuery {
        val insertRequestment = Requests.insert {
            it[Requests.requestId] = requestId
            it[Requests.title] = title
            it[Requests.artist] = artist
            it[Requests.singer] = singer
            it[Requests.requestTime] = requestTime
        }
        insertRequestment.resultedValues?.map { resultRowToRequest(it) }?.singleOrNull()
    }

    override suspend fun addNewRequestIgnore(
        requestId: Int,
        title: String,
        artist: String,
        singer: String,
        requestTime: LocalDateTime
    ): Request? = dbQuery {
        val insertRequestment = Requests.insertIgnore {
            it[Requests.requestId] = Requests.requestId
            it[Requests.title] = title
            it[Requests.artist] = artist
            it[Requests.singer] = singer
            it[Requests.requestTime] = requestTime
        }
        insertRequestment.resultedValues?.map { resultRowToRequest(it) }?.singleOrNull()
    }

    override suspend fun editRequest(
        requestId: Int,
        title: String,
        artist: String,
        singer: String,
        requestTime: LocalDateTime
    ): Boolean = dbQuery {
        Requests.update({ Requests.requestId eq requestId }) {
            it[Requests.requestId] = Requests.requestId
            it[Requests.title] = title
            it[Requests.artist] = artist
            it[Requests.singer] = singer
            it[Requests.requestTime] = requestTime
        } > 0
    }

    override suspend fun deleteRequest(id: Int): Boolean = dbQuery {
        Requests.deleteWhere { Requests.requestId eq id } > 0
    }
}