package ru.netology.nework.repository.posts

import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.dto.AttachmentType
import ru.netology.nework.dto.Post
import java.io.File

interface PostRepository {
    val data: Flow<PagingData<Post>>
    val dataUserWall: Flow<PagingData<Post>>
    val edited : MutableLiveData<Post>
    suspend fun getAll(authToken: String?)
    suspend fun getUserWall(authToken: String?, sentUserId: Int)
    suspend fun removeById(authToken: String, id: Int)
    suspend fun save(post: Post, authToken: String)
    suspend fun likeById(id: Int, willLike: Boolean, authToken: String, userId: Int): Post
    suspend fun saveWithAttachment(
        post: Post,
        file: File,
        authToken: String,
        attachmentType: AttachmentType
    )
}