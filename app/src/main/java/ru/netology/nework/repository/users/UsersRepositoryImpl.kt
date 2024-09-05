package ru.netology.nework.repository.users

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nework.api.ApiService
import ru.netology.nework.dto.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsersRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
) : UsersRepository {

    private val emptyUsers: List<User> = emptyList()

    private val _usersData = MutableLiveData(emptyUsers)
    override val usersData: LiveData<List<User>>
        get() = _usersData

    override suspend fun getUsers() {
        try {
            val response = apiService.getUsers()
            if (!response.isSuccessful) {
                throw RuntimeException(response.code().toString())
            }
            val users = response.body() ?: throw RuntimeException("body is null")
            _usersData.postValue(users)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    override suspend fun getBackOldUsers(oldUsers: List<User>) {
        _usersData.postValue(oldUsers)
    }

    override suspend fun checkCheckedUsers(ids: List<Int>) {
        val data = _usersData.value
        _usersData.postValue(data?.map {
            it.copy(
                checkedNow = if (ids.contains(it.id)) true else it.checkedNow
            )
        })
    }

    override suspend fun changeCheckedUsers(id: Int) {
        val data = _usersData.value
        _usersData.postValue(data?.map {
            it.copy(
                checkedNow = if (it.id == id)  !it.checkedNow else it.checkedNow
            )
        })
    }
}