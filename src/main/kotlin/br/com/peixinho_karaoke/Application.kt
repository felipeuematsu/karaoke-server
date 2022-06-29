package br.com.peixinho_karaoke

import br.com.peixinho_karaoke.database.RequestsDatabaseFactory
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import br.com.peixinho_karaoke.plugins.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "127.0.0.1") {
        RequestsDatabaseFactory.init()
        configureRouting()
        configureHTTP()
        configureSerialization()
    }.start(wait = true)
}
