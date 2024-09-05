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
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dto.*
import ru.netology.nework.repository.events.EventRepository
import ru.netology.nework.repository.users.UsersRepository
import ru.netology.nework.utils.SingleLiveEvent
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@HiltViewModel
class EventViewModel @Inject constructor(
    private val repository: EventRepository,
    private val usersRepository: UsersRepository,
    private val appAuth: AppAuth
) : ViewModel() {
    val edited = MutableLiveData(emptyEvent)

    @OptIn(ExperimentalCoroutinesApi::class)
    val data: Flow<PagingData<Event>> = appAuth
        .state
        .map { it?.id }
        .flatMapLatest { id ->
            repository.data.cachedIn(viewModelScope)
                .map { events ->
                    events.map { event ->
                        event.copy(ownedByMe = event.authorId == id)
                    }
                }
        }.flowOn(Dispatchers.Default)

    private val _dataState = MutableLiveData<FeedModelState>(FeedModelState.Idle)
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val _attachment = MutableLiveData(noMedia)
    val attachment: LiveData<MediaModel>
        get() = _attachment

    private val _eventCreated = SingleLiveEvent<Unit>()
    val eventCreated: LiveData<Unit>
        get() = _eventCreated
    private val _eventCreatedError = SingleLiveEvent<Pair<String, Event>>()
    val eventCreatedError: LiveData<Pair<String, Event>>
        get() = _eventCreatedError
    private val _eventsRemoveError = SingleLiveEvent<Pair<String, Int>>()
    val eventsRemoveError: LiveData<Pair<String, Int>>
        get() = _eventsRemoveError
    private val _eventsLikeError = SingleLiveEvent< Pair<Int, Boolean>>()
    val eventsLikeError: LiveData<Pair<Int, Boolean>>
        get() = _eventsLikeError
    private val _eventsParticipateError = SingleLiveEvent<Pair<Int, Boolean>>()
    val eventsParticipateError: LiveData<Pair<Int, Boolean>>
        get() = _eventsParticipateError
    private val _usersLoadError = SingleLiveEvent<String>()
    val usersLoadError: LiveData<String>
        get() = _usersLoadError

    init {
        load()
        loadUsers()
    }

    fun load() = viewModelScope.launch {
        _dataState.value = FeedModelState.Loading
        try {
            repository.getAll(appAuth.getToken())
            _dataState.value = FeedModelState.Idle
            Timber.i("Loaded events")
        } catch (e: Exception) {
            Timber.e("Error loading events: ${e.message}")
            _dataState.value = FeedModelState.Error
        }
    }

    fun loadUsers() = viewModelScope.launch {
        _dataState.value = FeedModelState.Loading
        try {
            usersRepository.getUsers()
            _dataState.value = FeedModelState.Idle
            Timber.i("Loaded users")
        } catch (e: Exception) {
            Timber.e("Error loading users: ${e.message}")
            _usersLoadError.postValue(e.toString())
        }
    }

    fun likeById(id: Int, likedByMe: Boolean) = viewModelScope.launch {
        try {
            appAuth.getToken()?.let { repository.likeById(id, !likedByMe, it, appAuth.getId()) }
            Timber.i("Like on event $id to ${!likedByMe}")
        } catch (e: Exception) {
            Timber.e("Error liking event: ${e.message}")
            _eventsLikeError.postValue(id to likedByMe)
        }
    }

    fun participateById(id: Int, participatedByMe: Boolean) = viewModelScope.launch {
        try {
            appAuth.getToken()
                ?.let { repository.participateById(id, !participatedByMe, it, appAuth.getId()) }
            Timber.i("Participation on event $id to ${!participatedByMe}")
        } catch (e: Exception) {
            Timber.e("Error participating in event: ${e.message}")
            _eventsParticipateError.postValue(id to participatedByMe)
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

    fun changeCoords(coords: Coords?){
        edited.value?.let {
            edited.value =
                it.copy(
                    coords = coords
                )
        }
    }

    fun changeEventType() {
        edited.value?.let {
            val newType = if (it.type == EventType.ONLINE) EventType.OFFLINE else EventType.ONLINE
            edited.value =
                it.copy(type = newType)
        }
    }

    fun changeEventDateTime(newDateTime: String) {
        edited.value?.let {
            edited.value =
                it.copy(datetime = newDateTime)
        }
    }


    fun changeSpeakerList(speakerList: List<Int>) {
        edited.value?.let {
            edited.value = it.copy(
                speakerIds = speakerList,
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
                            //we got new attachment
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
                    _eventCreated.postValue(Unit)
                    empty()
                    Timber.i("New event made")
                } catch (e: Exception) {
                    Timber.e("Error creating event: ${e.message}")
                    _eventCreatedError.postValue(e.message.toString() to it)
                }
            }
        }
    }

    fun edit(event: Event) {
        edited.value = event
    }

    fun empty() {
        edited.value = emptyEvent
        deleteMedia()
    }

    fun removeById(id: Int) = viewModelScope.launch {
        try {
            appAuth.getToken()?.let { repository.removeById(it, id) }
            Timber.i("Removed event $id")
        } catch (e: Exception) {
            Timber.e("Error removing event: ${e.message}")
            _eventsRemoveError.postValue(e.message.toString() to id)
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

private val emptyEvent = Event(
    id = 0,
    content = "",
    author = "Me",
    authorAvatar = null,
    published = "",
    datetime = "",
    type = EventType.OFFLINE,
)
private val noMedia = MediaModel(null, null, AttachmentType.NONE, null)