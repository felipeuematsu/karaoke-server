package br.com.felipeuematsu.service

import br.com.felipeuematsu.entity.Singer
import br.com.felipeuematsu.entity.SingerDTO
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
            }?.toDTO()
        }
}
