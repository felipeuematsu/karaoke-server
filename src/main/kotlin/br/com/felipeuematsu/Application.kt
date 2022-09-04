package br.com.felipeuematsu

import br.com.felipeuematsu.configuration.ApplicationConfiguration
import br.com.felipeuematsu.database.KaraokeDatabase
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import br.com.felipeuematsu.plugins.*

fun main() {
    embeddedServer(
        Netty,
        port = ApplicationConfiguration.serverPort,
        host = "127.0.0.1",
        watchPaths = listOf("build/classes/kotlin/main/br/com/felipeuematsu")
    ) {
        KaraokeDatabase.init()
        configureHTTP()
        configureRouting()
        configureSerialization()
    }.start(wait = true)
}
