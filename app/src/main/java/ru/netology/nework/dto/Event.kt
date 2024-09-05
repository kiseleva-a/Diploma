package ru.netology.nework.dto

import com.google.gson.annotations.SerializedName

enum class EventType {
    OFFLINE, ONLINE
}

data class Event (
    @SerializedName("id")
    val id: Int,
    @SerializedName("authorId")
    val authorId: Int = 0,
    @SerializedName("author")
    val author: String,
    @SerializedName("authorAvatar")
    val authorAvatar: String? = null,
    @SerializedName("authorJob")
    val authorJob: String? = null,
    @SerializedName("content")
    val content: String,
    @SerializedName("datetime")
    val datetime: String,
    @SerializedName("published")
    val published: String,
    @SerializedName("coords")
    val coords: Coords? = null,
    @SerializedName("type")
    val type: EventType,
    @SerializedName("likeOwnerIds")
    val likeOwnerIds: List<Int> = emptyList(),
    @SerializedName("likedByMe")
    val likedByMe: Boolean = false,
    @SerializedName("speakerIds")
    val speakerIds: List<Int> = emptyList(),
    @SerializedName("participantsIds")
    val participantsIds: List<Int> = emptyList(),
    @SerializedName("participatedByMe")
    val participatedByMe: Boolean = false,
    @SerializedName("attachment")
    val attachment: Attachment? = null,
    @SerializedName("link")
    val link: String? = null,
    @SerializedName("ownedByMe")
    val ownedByMe: Boolean = false,
    @SerializedName("users")
    val users: Map<Int,UserPreview> = emptyMap(),
)