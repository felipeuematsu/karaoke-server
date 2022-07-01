package br.com.peixinho_karaoke.models.dao

import br.com.peixinho_karaoke.models.State

interface StateDAO {
    suspend fun allStates(): List<State>
    suspend fun getState(id: Int): State?
    suspend fun deleteState(id: Int): Boolean
    suspend fun addNewState(accepting: Boolean, serial: Int): State?
    suspend fun addNewStateIgnore(accepting: Boolean, serial: Int): State?
    suspend fun addOrUpdateNewState(id: Int, accepting: Boolean, serial: Int): State?
    suspend fun editState(id: Int, accepting: Boolean, serial: Int): Boolean
}