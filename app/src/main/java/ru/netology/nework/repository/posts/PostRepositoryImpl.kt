package ru.netology.nework.repository.posts

import androidx.lifecycle.MutableLiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import kotlinx.coroutines.flow.map
import ru.netology.nework.api.ApiService
import ru.netology.nework.dao.PostDao
import ru.netology.nework.dao.PostRemoteKeyDao
import ru.netology.nework.db.AppDb
import ru.netology.nework.dto.*
import ru.netology.nework.entity.PostEntity
import ru.netology.nework.repository.media.MediaRepository
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val postDao: PostDao,
    private val apiService: ApiService,
    postRemoteKeyDao: PostRemoteKeyDao,
    appDb: AppDb,
    private val mediaRepository: MediaRepository
) : PostRepository {
    @OptIn(ExperimentalPagingApi::class)
    override val data = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        pagingSourceFactory = { postDao.getPagingSource() },
        remoteMediator = PostRemoteMediator(apiService, postDao, postRemoteKeyDao, appDb),
    ).flow
        .map { it.map(PostEntity::toDto) }


    var userId = 0
    @OptIn(ExperimentalPagingApi::class)
    override val dataUserWall = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        pagingSourceFactory = { postDao.getMyWalLPagingSource(userId) },
        remoteMediator = PostRemoteMediatorUserWall(
            apiService,
            postDao,
            postRemoteKeyDao,
            appDb,
            userId,
        ),
    ).flow
        .map { it.map(PostEntity::toDto) }

    override val edited = MutableLiveData(emptyPost)

    //POSTS
    override suspend fun getAll(authToken: String?) {
        val response = apiService.getAllPosts()
        if (!response.isSuccessful) {
            throw RuntimeException(response.code().toString())
        }
        val posts = response.body() ?: throw RuntimeException("body is null")
        postDao.insert(posts.map(PostEntity.Companion::fromDto))

        if (authToken != null) {
            postDao.getAllUnsent().forEach { save(it.toDto(), authToken) }
        }
    }

    override suspend fun removeById(authToken: String, id: Int) {
        val removed = postDao.getById(id)
        postDao.removeById(id)
        try {
            val response = apiService.removePostById(authToken, id)
            if (!response.isSuccessful) {
                postDao.save(removed)
                throw RuntimeException(response.code().toString())
            }
        } catch (e: Exception) {
            postDao.save(removed)
            throw RuntimeException(e)
        }
    }

    override suspend fun save(post: Post, authToken: String) {
        val mentionList =
            post.mentionIds.toList() //Hacky method, but for some reason ID conversion eats list
        postDao.save(PostEntity.fromDto(post, true))
        try {
            val response = apiService.savePost(authToken, post.copy(mentionIds = mentionList))
            if (!response.isSuccessful) {
                throw RuntimeException(
                    response.code().toString()
                )
            }
            val body = response.body() ?: throw RuntimeException("body is null")
            postDao.save(PostEntity.fromDto(body))
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    override suspend fun saveWithAttachment(
        post: Post,
        file: File,
        authToken: String,
        attachmentType: AttachmentType
    ) {
        try {
            val upload = mediaRepository.upload(file, authToken)
            val postWithAttachment =
                post.copy(attachment = Attachment(upload.url, attachmentType))
            save(postWithAttachment, authToken)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    override suspend fun likeById(
        id: Int,
        willLike: Boolean,
        authToken: String,
        userId: Int
    ): Post {
        postDao.likeById(id, userId)
        try {
            val response = if (willLike)
                apiService.likePostById(authToken, id)
            else
                apiService.dislikePostById(authToken, id)
            if (!response.isSuccessful) {
                postDao.likeById(id, userId)
                throw RuntimeException(response.code().toString())
            }
            return response.body() ?: throw RuntimeException("body is null")
        } catch (e: Exception) {
            postDao.likeById(id, userId)
            throw RuntimeException(e)
        }
    }

    //USER WALL
    override suspend fun getUserWall(authToken: String?, sentUserId: Int) {
        userId = sentUserId
        val response = apiService.getUserWall(userId)
        if (!response.isSuccessful) {
            throw RuntimeException(response.code().toString())
        }
        val posts = response.body() ?: throw RuntimeException("body is null")
        postDao.insert(posts.map(PostEntity.Companion::fromDto))

        authToken?.let {
            postDao.getAllUnsent().forEach { save(it.toDto(), authToken) }
        }
    }
}

private val emptyPost = Post(
    id = 0,
    content = "",
    author = "Me",
    authorAvatar = null,
    published = "",
)