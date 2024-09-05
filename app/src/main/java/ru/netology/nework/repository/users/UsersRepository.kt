package ru.netology.nework.repository.users

import androidx.lifecycle.LiveData
import ru.netology.nework.dto.User

interface UsersRepository {
    val usersData: LiveData<List<User>>
    suspend fun getUsers()
    suspend fun getBackOldUsers(oldUsers: List<User>)
    suspend fun checkCheckedUsers(ids: List<Int>)
    suspend fun changeCheckedUsers(id: Int)
}