package br.com.felipeuematsu

import io.ktor.server.application.Application
import br.com.felipeuematsu.configuration.ApplicationConfiguration
import br.com.felipeuematsu.database.KaraokeDatabase
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import br.com.felipeuematsu.plugins.*

fun main() {
    Runtime.getRuntime().exec("powershell /c start yt_resources\\flup_youtube_backend.exe")
    embeddedServer(
        Netty,
        port = ApplicationConfiguration.serverPort,
        watchPaths = listOf("build/classes/kotlin/main/br/com/felipeuematsu"),
        module = Application::myApplicationModule
    ).start(wait = true)
}

fun Application.myApplicationModule() {
    KaraokeDatabase.init()
    configureHTTP()
    configureRouting()
    configureSerialization()
}
