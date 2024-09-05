package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nework.api.ApiService
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dto.FeedModelState
import ru.netology.nework.dto.Job
import ru.netology.nework.utils.SingleLiveEvent
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class JobViewModel @Inject constructor(
    private val apiService: ApiService,
    private val appAuth: AppAuth,
) : ViewModel() {
    val edited = MutableLiveData(empty)

    private val _jobsData = MutableLiveData(emptyJobs)
    val jobsData: LiveData<List<Job>>
        get() = _jobsData
    private val _dataState = MutableLiveData<FeedModelState>(FeedModelState.Idle)
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val _newJobLoadError = SingleLiveEvent<String>()
    val newJobLoadError: LiveData<String>
        get() = _newJobLoadError

    private val _jobRemoveError = SingleLiveEvent<String>()
    val jobRemoveError: LiveData<String>
        get() = _jobRemoveError

    var userId: String? = "0"

    fun load(id: String?) {
        userId = id
        _dataState.value = FeedModelState.Loading
        try {
            if (userId != null && userId != "0") {
                loadPersonJobs(userId!!)
            } else {
                loadMyJobs()
            }
            _dataState.value = FeedModelState.Idle
        } catch (e: Exception) {
            Timber.e("Error loading user $id jobs: ${e.message}")
            _dataState.value = FeedModelState.Error
        }
    }

    fun loadMyJobs() = viewModelScope.launch {
        appAuth.getToken()?.let { token ->
            try {
                val response = apiService.getMyJobs(token)
                if (!response.isSuccessful) {
                    throw RuntimeException(response.code().toString())
                }
                val jobs = response.body() ?: throw RuntimeException("body is null")
                _jobsData.postValue(jobs.map { it.copy(ownedByMe = true) })
                Timber.i("Loaded my jobs")
            } catch (e: Exception) {
                Timber.e("Error loading my jobs: ${e.message}")
            }
        }
    }

    fun loadPersonJobs(id: String) = viewModelScope.launch {
        try {
            val response = apiService.getPersonJobs(id)
            if (!response.isSuccessful) {
                throw RuntimeException(response.code().toString())
            }
            val jobs = response.body() ?: throw RuntimeException("body is null")
            _jobsData.postValue(jobs)
            Timber.i("Loaded user $id jobs")
        } catch (e: Exception) {
            Timber.e("Error loading user $id jobs: ${e.message}")
        }
    }

    fun saveNewJob() = viewModelScope.launch {
        edited.value?.let {
            appAuth.getToken()?.let { token ->
                try {
                    val response = apiService.addNewJob(
                        token,
                        it
                    )
                    if (!response.isSuccessful) {
                        throw RuntimeException(response.code().toString())
                    } else {
                        load(userId)
                    }
                    Timber.i("Saved new job")
                } catch (e: Exception) {
                    Timber.e("Error saving new job: ${e.message}")
                    _newJobLoadError.postValue(e.toString())
                }
            }
        }
    }

    fun removeById(id: Int) = viewModelScope.launch {
        try {
            appAuth.getToken()?.let {
                val response = apiService.removeJob(it, id.toString())
                if (!response.isSuccessful) {
                    throw RuntimeException(response.code().toString())
                } else {
                    load(userId)
                }
            }
            Timber.i("Removed $id job")
        } catch (e: Exception) {
            Timber.e("Error removing $id job: ${e.message}")
            _jobRemoveError.postValue(e.message.toString())
        }
    }

    fun edit(job: Job) {
        edited.value = job
    }

    fun empty() {
        edited.value = empty
    }

    fun changeContent(
        name: String,
        position: String,
        start: String,
        finish: String?,
        link: String
    ) {
        edited.value?.let {
            val textName = name.trim()
            val textPosition = position.trim()
            val textLink = link.trim()
            edited.value =
                it.copy(
                    name = textName,
                    position = textPosition,
                    link = if (textLink.isNotBlank()) textLink else null,
                    start = start,
                    finish = finish
                )
        }
    }
}

private val empty = Job()
private val emptyJobs: List<Job> = emptyList()

