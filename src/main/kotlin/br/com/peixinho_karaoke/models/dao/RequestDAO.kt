package br.com.peixinho_karaoke.models.dao

import br.com.peixinho_karaoke.models.Request
import java.time.LocalDateTime

interface RequestDAO {
    suspend fun allRequests(): List<Request>
    suspend fun getRequest(id: Int): Request?
    suspend fun deleteRequest(id: Int): Boolean
    suspend fun addNewRequest(
        requestId: Int,
        title: String,
        artist: String,
        singer: String,
        keyChange: Int,
        requestTime: LocalDateTime
    ): Request?

    suspend fun addNewRequestIgnore(
        requestId: Int,
        title: String,
        artist: String,
        singer: String,
        keyChange: Int,
        requestTime: LocalDateTime
    ): Request?

    suspend fun editRequest(
        requestId: Int,
        title: String,
        artist: String,
        singer: String,
        keyChange: Int,
        requestTime: LocalDateTime
    ): Boolean
}