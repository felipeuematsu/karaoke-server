package dev.felipeuematsu.service

import dev.felipeuematsu.entity.Singer
import dev.felipeuematsu.entity.SingerDTO
import org.jetbrains.exposed.sql.transactions.transaction

object SingerService {
    fun addSinger(name: String): SingerDTO =
        transaction {
            Singer.new { this.name = name }.toDTO()
        }

    fun getSingers(): List<SingerDTO> =
        transaction {
            Singer.all().map { it.toDTO() }
        }

    fun updateSinger(singerDTO: SingerDTO): SingerDTO? =
        transaction {
            Singer.findById(singerDTO.id)?.apply {
                name = singerDTO.name
                active = singerDTO.active
            }?.toDTO()
        }

    fun deleteSinger(id: Int): Boolean = transaction {
        Singer.findById(id)?.delete() ?: return@transaction false
        return@transaction true
    }
}
