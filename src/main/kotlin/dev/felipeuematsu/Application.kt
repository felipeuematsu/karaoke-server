package dev.felipeuematsu

import io.ktor.server.application.Application
import dev.felipeuematsu.configuration.ApplicationConfiguration
import dev.felipeuematsu.database.KaraokeDatabase
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import dev.felipeuematsu.plugins.*

fun main() {
//    Runtime.getRuntime().exec("powershell /c start yt_resources\\flup_youtube_backend.exe")
    embeddedServer(
        Netty,
        port = ApplicationConfiguration.serverPort,
        watchPaths = listOf("build/classes/kotlin/main/dev/felipeuematsu"),
        module = Application::myApplicationModule
    ).start(wait = true)
}

fun Application.myApplicationModule() {
    KaraokeDatabase.init()
    configureHTTP()
    configureRouting()
    configureSerialization()
}
