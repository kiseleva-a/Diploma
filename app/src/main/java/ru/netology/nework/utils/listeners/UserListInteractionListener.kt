package ru.netology.nework.utils.listeners

import ru.netology.nework.dto.User

interface UserListInteractionListener {
    fun onClick(user: User)
}