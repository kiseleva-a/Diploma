package ru.netology.nework.dto

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id : Int,
    @SerializedName("login")
    val login: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("avatar")
    val avatar: String?,

    val checkedNow: Boolean = false
)
