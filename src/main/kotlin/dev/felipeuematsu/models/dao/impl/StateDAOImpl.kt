package dev.felipeuematsu.models.dao.impl

import dev.felipeuematsu.database.KaraokeDatabase.dbQuery
import dev.felipeuematsu.models.State
import dev.felipeuematsu.models.dao.StateDAO
import dev.felipeuematsu.models.States
import org.jetbrains.exposed.sql.*

class StateDAOImpl : StateDAO {
    private fun resultRowToState(row: ResultRow) = State(
        id = row[States.id],
        accepting = row[States.accepting],
        serial = row[States.serial],
    )

    override suspend fun allStates(): List<State> = dbQuery {
        States.selectAll().map(::resultRowToState).toList()
    }

    override suspend fun getState(id: Int): State? = dbQuery {
        States.select { States.id eq id }
            .map(::resultRowToState)
            .singleOrNull()
    }

    override suspend fun addNewState(accepting: Boolean, serial: Int): State? = dbQuery {
        val insertStatement = States.insert {
            it[States.accepting] = accepting
            it[States.serial] = serial
        }
        insertStatement.resultedValues?.map { resultRowToState(it) }?.singleOrNull()
    }

    override suspend fun addNewStateIgnore(accepting: Boolean, serial: Int): State? = dbQuery {
        val insertStatement = States.insertIgnore {
            it[States.accepting] = accepting
            it[States.serial] = serial
        }
        insertStatement.resultedValues?.map { resultRowToState(it) }?.singleOrNull()
    }

    override suspend fun addOrUpdateNewState(id: Int, accepting: Boolean, serial: Int): State? {
        try {
            val insertStatement = States.insert {
                it[States.id] = id
                it[States.accepting] = accepting
                it[States.serial] = serial
            }
            return insertStatement.resultedValues?.map { resultRowToState(it) }?.singleOrNull()
        } catch (e: Exception) {
            val updatedId = States.update {
                it[States.id] = id
                it[States.accepting] = accepting
                it[States.serial] = serial
            }
            return getState(updatedId)
        }
    }

    override suspend fun editState(id: Int, accepting: Boolean, serial: Int): Boolean = dbQuery {
        States.update({ States.id eq id }) {
            it[States.id] = id
            it[States.accepting] = accepting
            it[States.serial] = serial

        } > 0
    }

    override suspend fun deleteState(id: Int): Boolean = dbQuery {
        States.deleteWhere { States.id eq id } > 0
    }
}
