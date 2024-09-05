package ru.netology.nework.repository.events

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import kotlinx.coroutines.flow.map
import ru.netology.nework.api.ApiService
import ru.netology.nework.dao.EventDao
import ru.netology.nework.dao.EventRemoteKeyDao
import ru.netology.nework.db.AppDb
import ru.netology.nework.dto.*
import ru.netology.nework.entity.EventEntity
import ru.netology.nework.repository.media.MediaRepository
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao,
    private val apiService: ApiService,
    eventRemoteKeyDao: EventRemoteKeyDao,
    appDb: AppDb,
    private val mediaRepository: MediaRepository
) : EventRepository {
    @OptIn(ExperimentalPagingApi::class)
    override val data = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        pagingSourceFactory = { eventDao.getPagingSource() },
        remoteMediator = EventRemoteMediator(apiService, eventDao, eventRemoteKeyDao, appDb),
    ).flow
        .map { it.map(EventEntity::toDto) }

    //EVENTS
    override suspend fun getAll(authToken: String?) {
        val response = apiService.getAllEvents()
        if (!response.isSuccessful) {
            throw RuntimeException(response.code().toString())
        }
        val events = response.body() ?: throw RuntimeException("body is null")
        eventDao.insert(events.map(EventEntity.Companion::fromDto))

        if (authToken != null) {
            eventDao.getAllUnsent().forEach { save(it.toDto(), authToken) }
        }
    }

    override suspend fun removeById(authToken: String, id: Int) {
        val removed = eventDao.getById(id)
        eventDao.removeById(id)
        try {
            val response = apiService.removeEventById(authToken, id)
            if (!response.isSuccessful) {
                eventDao.save(removed)
                throw RuntimeException(response.code().toString())
            }
        } catch (e: Exception) {
            eventDao.save(removed)
            throw RuntimeException(e)
        }
    }

    override suspend fun save(event: Event, authToken: String) {
//        val mentionList =
//            event.mentionIds.toList() //Hacky method, but for some reason ID conversion eats list
        eventDao.save(EventEntity.fromDto(event, true))
        try {
            val response = apiService.saveEvent(authToken, event)//.copy(mentionIds = mentionList))
            if (!response.isSuccessful) {
                throw RuntimeException(
                    response.code().toString()
                )
            }
            val body = response.body() ?: throw RuntimeException("body is null")
            eventDao.save(EventEntity.fromDto(body))
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    override suspend fun saveWithAttachment(
        event: Event,
        file: File,
        authToken: String,
        attachmentType: AttachmentType
    ) {
        try {
            val upload = mediaRepository.upload(file, authToken)
            val eventWithAttachment =
                event.copy(attachment = Attachment(upload.url, attachmentType))
            save(eventWithAttachment, authToken)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    override suspend fun likeById(
        id: Int,
        willLike: Boolean,
        authToken: String,
        userId: Int
    ): Event {
        eventDao.likeById(id, userId)
        try {
            val response = if (willLike)
                apiService.likeEventById(authToken, id)
            else
                apiService.dislikeEventById(authToken, id)
            if (!response.isSuccessful) {
                eventDao.likeById(id, userId)
                throw RuntimeException(response.code().toString())
            }
            return response.body() ?: throw RuntimeException("body is null")
        } catch (e: Exception) {
            eventDao.likeById(id, userId)
            throw RuntimeException(e)
        }
    }

    override suspend fun participateById(
        id: Int,
        willParticipate: Boolean,
        authToken: String,
        userId: Int
    ): Event {
        eventDao.participateById(id, userId)
        try {
            val response = if (willParticipate)
                apiService.participateEventById(authToken, id)
            else
                apiService.unparticipateEventById(authToken, id)
            if (!response.isSuccessful) {
                eventDao.participateById(id, userId)
                throw RuntimeException(response.code().toString())
            }
            return response.body() ?: throw RuntimeException("body is null")
        } catch (e: Exception) {
            eventDao.participateById(id, userId)
            throw RuntimeException(e)
        }
    }
}