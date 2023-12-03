package dev.felipeuematsu.entity

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

@Serializable
data class SingerDTO(
    val id: Int = 0,
    val name: String,
    val active: Boolean
)

class Singer(id: EntityID<Int>) : IntEntity(id) {
    fun toDTO() = SingerDTO(id.value, name, active)

    companion object : IntEntityClass<Singer>(Singers)

    var name by Singers.name
    var active by Singers.active
}

object Singers : IntIdTable("historySingers") {
    val name = text("Name").uniqueIndex()
    val active = bool("Active").default(true)
}