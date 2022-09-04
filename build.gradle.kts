val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val exposed_version: String by project
val sqlite_version: String by project
val ktorm_version: String by project
val ktor_client: String by project
val ktor_client_core: String by project
val ktor_server_websockets: String by project
val tika_version: String by project

plugins {
    application
    kotlin("jvm") version "1.7.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.7.0"
}

group = "br.com.felipeuematsu"
version = "0.0.1"
application {
    mainClass.set("br.com.felipeuematsu.ApplicationKt")

    val isDevelopment: Boolean = true
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
}
dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-cors-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposed_version")
    implementation("org.xerial:sqlite-jdbc:$sqlite_version")
    implementation("org.ktorm:ktorm-core:$ktorm_version")
    implementation("io.ktor:ktor-client:$ktor_client")
    implementation("io.ktor:ktor-client-core:$ktor_client_core")
    implementation("io.ktor:ktor-client-cio:$ktor_client_core")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_client_core")
    implementation("io.ktor:ktor-server-websockets:$ktor_server_websockets")
    implementation("org.apache.tika:tika-parser-audiovideo-module:$tika_version")
    implementation("org:jaudiotagger:2.0.3")
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}
