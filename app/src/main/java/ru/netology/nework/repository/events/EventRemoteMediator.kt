package ru.netology.nework.repository.events

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import ru.netology.nework.api.ApiService
import ru.netology.nework.dao.EventDao
import ru.netology.nework.dao.EventRemoteKeyDao
import ru.netology.nework.db.AppDb
import ru.netology.nework.entity.EventEntity
import ru.netology.nework.entity.EventRemoteKeyEntity
import ru.netology.nework.error.ApiError
import timber.log.Timber

@OptIn(ExperimentalPagingApi::class)
class EventRemoteMediator(
    private val service: ApiService,
    private val eventDao: EventDao,
    private val eventRemoteKeyDao: EventRemoteKeyDao,
    private val appDb: AppDb,
) : RemoteMediator<Int, EventEntity>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, EventEntity>
    ): MediatorResult {
        try {
            val maxId = eventRemoteKeyDao.max()
            val result = when (loadType) {
                LoadType.REFRESH -> {
                    if (maxId == null) {
                        service.getLatestEvents(state.config.pageSize)
                    } else {
                        val id = eventRemoteKeyDao.max() ?: return MediatorResult.Success(false)
                        service.getEventsAfter(id.toString(), state.config.pageSize)
                    }
                }
                LoadType.APPEND -> {
                    val id = eventRemoteKeyDao.min() ?: return MediatorResult.Success(false)
                    service.getEventsBefore(id.toString(), state.config.pageSize)
                }
                LoadType.PREPEND -> {
                    return MediatorResult.Success(false)
                }
            }

            if (!result.isSuccessful) {
                throw ApiError(result.code(), result.message())
            }

            val body = result.body() ?: throw ApiError(
                result.code(),
                result.message(),
            )

            appDb.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {
                        if (maxId == null) {
                            eventDao.clear()
                            eventRemoteKeyDao.insert(
                                listOf(
                                    EventRemoteKeyEntity(
                                        EventRemoteKeyEntity.KeyType.AFTER,
                                        body.first().id
                                    ),
                                    EventRemoteKeyEntity(
                                        EventRemoteKeyEntity.KeyType.BEFORE,
                                        body.last().id
                                    ),
                                )
                            )
                        } else {
                            eventRemoteKeyDao.insert(
                                EventRemoteKeyEntity(
                                    EventRemoteKeyEntity.KeyType.AFTER,
                                    body.first().id
                                ),
                            )
                        }
                    }
                    LoadType.PREPEND -> {
                    }
                    LoadType.APPEND -> {
                        eventRemoteKeyDao.insert(
                            EventRemoteKeyEntity(
                                EventRemoteKeyEntity.KeyType.BEFORE,
                                body.last().id
                            ),
                        )
                    }
                }

                eventDao.insert(body.map(EventEntity::fromDto))
            }
            return MediatorResult.Success(body.isEmpty())
        } catch (e: Exception) {
            Timber.e("Error loading more events: ${e.message}")
            return MediatorResult.Error(e)
        }
    }
}
