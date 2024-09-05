package ru.netology.nework.repository.media

import ru.netology.nework.dto.MediaUpload
import java.io.File

interface MediaRepository {
    suspend fun upload(file: File, authToken: String): MediaUpload
}