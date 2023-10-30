package dev.felipeuematsu.configuration

object ApplicationConfiguration {
    val serverPort = System.getenv("PORT")?.toInt() ?: 80
}