package dev.felipeuematsu.entity

import dev.felipeuematsu.entity.PlaylistSongs.references
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

@Serializable
data class CurrentSingerDTO(
    val id: Int,
    val name: String,
    val position: Int,
    val singerId: Int,

    @Serializable(with = InstantSerializer::class) val addts: Instant,
)

class CurrentSinger(id: EntityID<Int>) : IntEntity(id) {
    fun toDTO() = CurrentSingerDTO(id.value, name, position, singerId.value, addts)

    companion object : IntEntityClass<CurrentSinger>(Singers)

    var name by CurrentSingers.name
    var position by CurrentSingers.position
    var addts by CurrentSingers.addts
    var singerId by CurrentSingers.singer references Singers.id

}

object CurrentSingers : IntIdTable("historySingers") {
    val name = text("name").uniqueIndex("sqlite_autoindex_regularSingers_1")
    val position = integer("position")
    val addts = timestamp("addts")
    val singer = reference("SingerId", Singers)
}