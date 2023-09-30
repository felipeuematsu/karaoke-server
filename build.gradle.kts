import groovy.json.JsonSlurper
import java.net.URL
import java.util.Base64
import java.util.zip.ZipFile

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
val spa_version: String by project
val yt_server_version: String by project

plugins {
    application
    kotlin("jvm") version "1.7.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.7.0"
}

group = "dev.felipeuematsu"
version = "1.5.1"
application {
    mainClass.set("br.com.felipeuematsu.ApplicationKt")
}

distributions {
    main {
        contents {
            from("flutter_resources") {
                into("bin/flutter_resources")
            }
            from("yt_resources") {
                into("bin/yt_resources")
            }
            from("resources") {
                into("bin/resources")
            }
        }
    }
}

tasks {
    build {
        dependsOn("setupSpa")
        dependsOn("setupYTServer")
    }
    installDist {
        dependsOn("setupSpa")
        dependsOn("setupYTServer")
    }
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

abstract class SetupSpaTask : DefaultTask() {
    @get:Input
    abstract val spaVersion: Property<String>

    @TaskAction
    fun setupSpa() {
        val username = "felipeuematsu"
        val password = System.getenv("API_PASSWORD")

        val token = Base64.getEncoder().encodeToString("$username:$password".toByteArray())
        val releasesUrl = "https://api.github.com/repos/felipeuematsu/flup_karaoke/releases"

        val spaDir = File("flutter_resources")

        val dataResponse = URL(releasesUrl).openConnection().apply {
            setRequestProperty(
                "Authorization",
                "Basic $token"
            )
        }.getInputStream().readBytes().toString(Charsets.UTF_8)
        val json = JsonSlurper().parseText(dataResponse) as List<Map<String, Any>>

        val release = json.firstOrNull { it["tag_name"] == spaVersion.get() }
            ?: return println("No release found for version ${spaVersion.get()}")

        val assets = release["assets"] as List<Map<String, String>>
        val asset = assets.firstOrNull { it["name"]?.contains(".zip") == true }
            ?: throw Exception("No release file found for version ${spaVersion.get()}")
        val assetUrl = asset["browser_download_url"] ?: throw Exception("Asset not found")

        if (!spaDir.exists()) {
            println("Creating flutter_resources directory")
            spaDir.mkdirs()
            println("flutter_resources directory created successfully")
        }

        val currentDir = System.getProperty("user.dir")
        val zipFile = File("${currentDir}/temp/web.zip")
        val lockFile = File("${currentDir}/temp/web.lock")
        if (!zipFile.exists() || !lockFile.exists() || (lockFile.readText() != spaVersion.get())) {
            println("Creating Dirs")
            zipFile.parentFile.mkdirs()
            println("Downloading SPA")
            zipFile.createNewFile()
            if (!lockFile.exists()) {
                lockFile.createNewFile()
            }
            println("Downloading SPA version ${spaVersion.get()}")
            println(assetUrl)
            val assetDownloadUrl = URL(assetUrl)
            val inputStream = assetDownloadUrl.openConnection().getInputStream()

            println("Writing to file")
            zipFile.writeBytes(inputStream.readBytes())

            lockFile.writeText(spaVersion.get())
            println("Downloaded SPA version ${spaVersion.get()}")
        }

        println("Extracting SPA version ${spaVersion.get()}")

        val zip = ZipFile(zipFile)
        zip.entries().asSequence().forEach { entry ->
            val fileName = entry.name.replace("build/web/", "")
            val file = File(spaDir, fileName)
            if (entry.isDirectory) {
                file.mkdirs()
            } else {
                file.outputStream().use { zip.getInputStream(entry).copyTo(it) }
            }
        }
        println("Extracted SPA version ${spaVersion.get()}")
    }
}
tasks.register<SetupSpaTask>("setupSpa") { spaVersion.set(spa_version) }

abstract class SetupYTServerTask : DefaultTask() {
    @get:Input
    abstract val ytServerVersion: Property<String>

    @TaskAction
    fun setupSpa() {
        val username = "felipeuematsu"
        val password = System.getenv("API_PASSWORD")

        val token = Base64.getEncoder().encodeToString("$username:$password".toByteArray())
        val releasesUrl = "https://api.github.com/repos/felipeuematsu/flup_youtube_backend/releases"

        val ytDir = File("yt_resources")

        val dataResponse = URL(releasesUrl).openConnection().apply {
            setRequestProperty(
                "Authorization",
                "Basic $token"
            )
        }.getInputStream().readBytes().toString(Charsets.UTF_8)
        val json = JsonSlurper().parseText(dataResponse) as List<Map<String, Any>>

        val release = json.firstOrNull { it["tag_name"] == ytServerVersion.get() }
            ?: return println("No release found for version ${ytServerVersion.get()}")

        val assets = release["assets"] as List<Map<String, String>>
        val asset = assets.firstOrNull { it["name"]?.contains(".exe") == true }
            ?: throw Exception("No release file found for version ${ytServerVersion.get()}")
        val assetUrl = asset["browser_download_url"] ?: throw Exception("Asset not found")

        if (!ytDir.exists()) {
            println("Creating yt_executable directory")
            ytDir.mkdirs()
            println("yt_executable directory created successfully")
        }

        val tempDir = System.getProperty("user.dir")
        val exeFile = File("${tempDir}/temp/flup_youtube_backend.exe")
        val lockFile = File("${tempDir}/temp/flup_youtube_backend.lock")
        if (!exeFile.exists() || !lockFile.exists() || (lockFile.readText() != ytServerVersion.get())) {
            println("Creating Dirs")
            exeFile.parentFile.mkdirs()
            println("Downloading YT")
            exeFile.createNewFile()
            if (!lockFile.exists()) {
                lockFile.createNewFile()
            }
            println("Downloading YT version ${ytServerVersion.get()}")
            println(assetUrl)
            val assetDownloadUrl = URL(assetUrl)
            val inputStream = assetDownloadUrl.openConnection().getInputStream()

            println("Writing to file")
            exeFile.writeBytes(inputStream.readBytes())

            lockFile.writeText(ytServerVersion.get())
            println("Downloaded YT version ${ytServerVersion.get()}")
        }

        println("Copying YT version ${ytServerVersion.get()}")

        val ytFile = File(ytDir, "flup_youtube_backend.exe")
        ytFile.writeBytes(exeFile.readBytes())
        println("Copied YT version ${ytServerVersion.get()}")

    }
}
tasks.register<SetupYTServerTask>("setupYTServer") { ytServerVersion.set(yt_server_version) }
