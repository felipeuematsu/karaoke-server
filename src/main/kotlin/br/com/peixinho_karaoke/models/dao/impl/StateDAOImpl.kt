package br.com.peixinho_karaoke.models.dao.impl

import br.com.peixinho_karaoke.database.RequestsDatabaseFactory.dbQuery
import br.com.peixinho_karaoke.models.State
import br.com.peixinho_karaoke.models.dao.StateDAO
import br.com.peixinho_karaoke.models.States
import kotlinx.coroutines.runBlocking
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

    override suspend fun addNewState(id: Int, accepting: Boolean, serial: Int): State? = dbQuery {
        val insertStatement = States.insert {
            it[States.id] = id
            it[States.accepting] = accepting
            it[States.serial] = serial
        }
        insertStatement.resultedValues?.map { resultRowToState(it) }?.singleOrNull()
    }

    override suspend fun addNewStateIgnore(id: Int, accepting: Boolean, serial: Int): State? = dbQuery {
        val insertStatement = States.insertIgnore {
            it[States.id] = id
            it[States.accepting] = accepting
            it[States.serial] = serial
        }
        insertStatement.resultedValues?.map { resultRowToState(it) }?.singleOrNull()
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

val stateDao: StateDAO = StateDAOImpl().apply {
    runBlocking {
        addNewStateIgnore(0, false, 1)
    }
}