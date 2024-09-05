package ru.netology.nework.repository.media

import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nework.api.ApiService
import ru.netology.nework.dto.MediaUpload
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
): MediaRepository {
    override suspend fun upload(file: File, authToken: String): MediaUpload {
        try {
            val data =
                MultipartBody.Part.createFormData("file", file.name, file.asRequestBody())

            val response = apiService.upload(authToken, data)
            if (!response.isSuccessful) {
                throw RuntimeException(
                    response.code().toString()
                )
            }
            Timber.i("File uploaded")
            return response.body() ?: throw RuntimeException("body is null")
        } catch (e: Exception) {
            Timber.e("Error uploading file: ${e.message}")
            throw RuntimeException(e)
        }
    }
}