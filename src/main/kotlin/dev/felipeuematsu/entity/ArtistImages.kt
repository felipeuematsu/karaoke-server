package dev.felipeuematsu.entity

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

data class ArtistImageDTO(val id: Int, val url: String, val name: String)

class ArtistImage(id: EntityID<Int>) : IntEntity(id) {
    fun toDTO() = ArtistImageDTO(id.value, url, name)

    companion object : IntEntityClass<ArtistImage>(ArtistImages)

    var name by ArtistImages.name
    var url by ArtistImages.url
}

object ArtistImages : IntIdTable() {
    val name = varchar("name", 50)
    val url = varchar("url", 500)
}