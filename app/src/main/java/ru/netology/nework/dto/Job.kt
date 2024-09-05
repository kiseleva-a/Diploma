package ru.netology.nework.dto

import com.google.gson.annotations.SerializedName

data class Job(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("name")
    val name: String = "",
    @SerializedName("position")
    val position: String = "",
    @SerializedName("start")
    val start: String= "",
    @SerializedName("finish")
    val finish: String? = null,
    @SerializedName("link")
    val link: String? = null,

    val ownedByMe : Boolean = false,
)
