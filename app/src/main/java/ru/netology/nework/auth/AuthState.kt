package ru.netology.nework.auth

data class AuthState(
    val id: Int = 0,
    val token: String? = null,
)