package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nework.dto.Coords
import ru.netology.nework.dto.FeedModelState
import ru.netology.nework.repository.users.UsersRepository
import ru.netology.nework.utils.SingleLiveEvent
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class UsersAndMapViewModel @Inject constructor(
    private val usersRepository: UsersRepository,
) : ViewModel() {
    val usersData = usersRepository.usersData

    private val _userIdList = MutableLiveData<MutableList<Int>>()
    val userIdList: LiveData<MutableList<Int>>
        get() = _userIdList

    private val _dataState = MutableLiveData<FeedModelState>(FeedModelState.Idle)
    val dataState: LiveData<FeedModelState>
        get() = _dataState
    private val _usersLoadError = SingleLiveEvent<String>()
    val usersLoadError: LiveData<String>
        get() = _usersLoadError

    var coords : Coords? = null

    init {
        loadUsers()
    }

    fun getUserList(list: List<Int>) {
        _userIdList.value = list.toMutableList()
    }

    fun loadUsers() = viewModelScope.launch {
        _dataState.value = FeedModelState.Loading
        try {
            usersRepository.getUsers()
            _dataState.value = FeedModelState.Idle
            Timber.i("Loaded users")
        } catch (e: Exception) {
            Timber.e("Error loading users: ${e.message}")
            _dataState.value = FeedModelState.Error
            _usersLoadError.postValue(e.toString())
        }
    }

    fun addUser(id: Int) {
        _userIdList.value?.add(id)
    }

    fun removeUser(id: Int) {
        _userIdList.value?.remove(id)
    }

    fun changeCheckedUsers(id: Int) = viewModelScope.launch {
        usersRepository.changeCheckedUsers(id)
    }

    fun checkCheckedUser(ids: List<Int>) = viewModelScope.launch {
        usersRepository.checkCheckedUsers(ids)
    }

/*  This part just doesn't work for some reason
    It should return back starting user list if we press back instead of Add butonn
    But navigation always starts before getBackOldUsers so this changes doesn't count for some reason

    private var oldUserList = emptyList<User>()
    private var oldIdList = emptyList<Int>()

    fun saveOldUsers() {
        if (userIdList.value != null) {
            oldIdList = userIdList.value!!.toList()
        }
        if (usersData.value != null) {
            oldUserList = usersData.value!!.toList()
        }
    }

    fun getBackOldUsers() = viewModelScope.launch {
        _userIdList.value = oldIdList.toMutableList()
        usersRepository.getBackOldUsers(oldUserList)
    }*/
}