package br.com.peixinho_karaoke

import br.com.peixinho_karaoke.configuration.ApplicationConfiguration
import br.com.peixinho_karaoke.database.RequestsDatabaseFactory
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import br.com.peixinho_karaoke.plugins.*

fun main() {
    embeddedServer(Netty, port = ApplicationConfiguration.serverPort, host = "127.0.0.1") {
        RequestsDatabaseFactory.init()
        configureRouting()
        configureHTTP()
        configureSerialization()
    }.start(wait = true)
}
