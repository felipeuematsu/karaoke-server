package br.com.felipeuematsu.configuration

object ApplicationConfiguration {
    val serverPort = System.getenv("PORT")?.toInt() ?: 80
}