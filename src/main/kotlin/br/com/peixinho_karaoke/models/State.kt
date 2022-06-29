package br.com.peixinho_karaoke.models

import org.jetbrains.exposed.sql.Table

data class State(
    val id: Int,
    val accepting: Boolean,
    val serial: Int,
)

object States : Table("state") {
    val id = integer("id").autoIncrement()
    val accepting = bool("accepting")
    val serial = integer("serial")

    override val primaryKey = PrimaryKey(id)

}