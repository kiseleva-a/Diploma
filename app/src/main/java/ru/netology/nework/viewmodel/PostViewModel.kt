package ru.netology.nework.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nework.api.ApiService
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dto.*
import ru.netology.nework.repository.posts.PostRepository
import ru.netology.nework.utils.SingleLiveEvent
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@HiltViewModel
open class PostViewModel @Inject constructor(
    protected val repository: PostRepository,
    protected val apiService: ApiService,
    protected val appAuth: AppAuth
) : ViewModel() {
    val edited = repository.edited

    @OptIn(ExperimentalCoroutinesApi::class)
    open val data: Flow<PagingData<Post>> = appAuth
        .state
        .map { it?.id }
        .flatMapLatest { id ->
            repository.data.cachedIn(viewModelScope)
                .map { posts ->
                    posts.map { post ->
                        post.copy(ownedByMe = post.authorId == id)
                    }
                }
        }.flowOn(Dispatchers.Default)

    protected val _dataState = MutableLiveData<FeedModelState>(FeedModelState.Idle)
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    protected val _attachment = MutableLiveData(noMedia)
    val attachment: LiveData<MediaModel>
        get() = _attachment

    protected val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated
    protected val _postCreatedError = SingleLiveEvent<Pair<String, Post>>()
    val postCreatedError: LiveData<Pair<String, Post>>
        get() = _postCreatedError
    protected val _postsRemoveError = SingleLiveEvent<Pair<String, Int>>()
    val postsRemoveError: LiveData<Pair<String, Int>>
        get() = _postsRemoveError
    protected val _postsLikeError = SingleLiveEvent<Pair<Int, Boolean>>()
    val postsLikeError: LiveData<Pair<Int, Boolean>>
        get() = _postsLikeError

    init {
        load()
    }

    open fun load() = viewModelScope.launch {
        _dataState.value = FeedModelState.Loading
        try {
            repository.getAll(appAuth.getToken())
            _dataState.value = FeedModelState.Idle
            Timber.i("Loaded posts")
        } catch (e: Exception) {
            Timber.e("Error loading posts: ${e.message}")
            _dataState.value = FeedModelState.Error
        }
    }

    fun likeById(id: Int, likedByMe: Boolean) = viewModelScope.launch {
        try {
            appAuth.getToken()?.let { repository.likeById(id, !likedByMe, it, appAuth.getId()) }
            Timber.i("Like on posts $id to ${!likedByMe}")
        } catch (e: Exception) {
            Timber.e("Error liking post: ${e.message}")
            _postsLikeError.postValue(id to likedByMe)
        }
    }

    fun changeContent(
        content: String,
        link: String,
    ) {
        edited.value?.let {
            val text = content.trim()
            val textLink = link.trim()
            edited.value =
                it.copy(
                    content = text,
                    link = if (textLink.isNotBlank()) textLink else null,
                )
        }
    }

    fun changeCoords(coords: Coords?) {
        edited.value?.let {
            edited.value =
                it.copy(
                    coords = coords
                )
        }
    }

    fun changeMentionedList(mentionedList: List<Int>) {
        edited.value?.let {
            edited.value = it.copy(
                mentionIds = mentionedList,
                mentionedMe = mentionedList.contains(appAuth.getId())
            )
        }
    }

    fun save() = viewModelScope.launch {
        edited.value?.let {
            appAuth.getToken()?.let { token ->
                try {
                    if (_attachment.value == noMedia) {
                        //if we have no attachment or we deleted it in edit
                        repository.save(it.copy(attachment = null), token)
                    } else {
                        if (_attachment.value!!.url != null) {
                            //if we edit and had an attachment and didn't change anything
                            repository.save(it, token)
                        } else {
                            _attachment.value?.file?.let { file ->
                                repository.saveWithAttachment(
                                    it,
                                    file,
                                    token,
                                    _attachment.value!!.attachmentType
                                )
                            }
                        }
                    }
                    _postCreated.postValue(Unit)
                    empty()
                    Timber.i("New post made")
                } catch (e: Exception) {
                    Timber.e("Error creating post: ${e.message}")
                    _postCreatedError.postValue(e.message.toString() to it)
                }
            }
        }
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun empty() {
        edited.value = emptyPost
        deleteMedia()
    }

    fun removeById(id: Int) = viewModelScope.launch {
        try {
            appAuth.getToken()?.let { repository.removeById(it, id) }
            Timber.i("Removed post $id")
        } catch (e: Exception) {
            Timber.e("Error removing post: ${e.message}")
            _postsRemoveError.postValue(e.message.toString() to id)
        }
    }

    fun changeMedia(
        fileUri: Uri?,
        toFile: File?,
        attachmentType: AttachmentType,
        url: String? = null
    ) {
        _attachment.value = MediaModel(fileUri, toFile, attachmentType, url)
    }

    fun deleteMedia() {
        _attachment.value = noMedia
    }
}

private val emptyPost = Post(
    id = 0,
    content = "",
    author = "Me",
    authorAvatar = null,
    published = "",
)
private val noMedia = MediaModel(null, null, AttachmentType.NONE, null)


