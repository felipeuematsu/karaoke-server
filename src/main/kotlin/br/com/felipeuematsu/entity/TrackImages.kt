package br.com.felipeuematsu.entity

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

data class TrackImageDTO(
    val id: Int,
    val url: String,
    val name: String,
    val artist: String,
)

class TrackImage(id: EntityID<Int>) : IntEntity(id) {
    fun toDTO() = TrackImageDTO(id.value, url, title, artist)

    companion object : IntEntityClass<TrackImage>(TrackImages)

    var title by TrackImages.title
    var artist by TrackImages.artist
    var url by TrackImages.url
}

object TrackImages : IntIdTable() {
    val title = varchar("title", 127)
    val artist = varchar("artist", 127)
    val url = varchar("url", 500)
}