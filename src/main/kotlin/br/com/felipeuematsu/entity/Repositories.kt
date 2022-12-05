package br.com.felipeuematsu.entity

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

@Serializable
data class RepositoryDTO(
    val path: String,
    val regex: String,
    val titlePos: Int,
    val artistPos: Int,
)

class Repository(id: EntityID<Int>) : IntEntity(id) {
    fun toDTO() = RepositoryDTO(
        path = path,
        regex = regex,
        titlePos = titlePos,
        artistPos = artistPos,
    )

    companion object : IntEntityClass<Repository>(Repositories)

    var path by Repositories.path
    var regex by Repositories.regex
    var titlePos by Repositories.titlePos
    var artistPos by Repositories.artistPos
}

object Repositories : IntIdTable("repositories") {
    val path = varchar("path", 255)
    val regex = varchar("regex", 255)
    val titlePos = integer("title_pos")
    val artistPos = integer("artist_pos")
}