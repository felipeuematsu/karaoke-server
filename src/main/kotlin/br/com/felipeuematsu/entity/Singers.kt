package br.com.felipeuematsu.entity

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

@Serializable
data class SingerDTO(
    val id: Int = 0,
    val name: String,
)

class Singer(id: EntityID<Int>) : IntEntity(id) {
    fun toDTO() = SingerDTO(id.value, name)

    companion object : IntEntityClass<Singer>(Singers)

    var name by Singers.name
}

object Singers : IntIdTable("historySingers") {
    val name = text("Name").uniqueIndex()
}