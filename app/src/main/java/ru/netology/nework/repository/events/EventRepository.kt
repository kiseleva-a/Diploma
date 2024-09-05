package ru.netology.nework.repository.events

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.dto.AttachmentType
import ru.netology.nework.dto.Event
import java.io.File

interface EventRepository {
    val data: Flow<PagingData<Event>>
    suspend fun getAll(authToken: String?)
    suspend fun removeById(authToken: String, id: Int)
    suspend fun save(event: Event, authToken: String)
    suspend fun likeById(id: Int, willLike: Boolean, authToken: String, userId: Int): Event
    suspend fun participateById(id: Int, willParticipate: Boolean, authToken: String, userId: Int): Event
    suspend fun saveWithAttachment(
        event: Event,
        file: File,
        authToken: String,
        attachmentType: AttachmentType
    )
}