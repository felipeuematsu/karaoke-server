package dev.felipeuematsu.service

import io.ktor.http.content.*
import java.io.File

object ImageService {
    fun getUserImage(userId: Int): File {
        return File("./resources/images/$userId.png")
    }

    fun setUserImage(userId: Int, image: PartData.FileItem) {
        val file = File("./resources/images/$userId.png")
        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()
        image.streamProvider().use { input ->
            file.outputStream().buffered().use { output ->
                input.copyTo(output)
            }
        }

    }
}