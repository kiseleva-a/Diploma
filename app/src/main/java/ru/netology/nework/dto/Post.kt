package ru.netology.nework.dto

import com.google.gson.annotations.SerializedName

data class Post (
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
    @SerializedName("published")
    val published: String,
    @SerializedName("coords")
    val coords: Coords? = null,
    @SerializedName("link")
    val link: String? = null,
    @SerializedName("likeOwnerIds")
    val likeOwnerIds: List<Int> = emptyList(),
    @SerializedName("mentionIds")
    val mentionIds: List<Int> = emptyList(),
    @SerializedName("mentionedMe")
    val mentionedMe: Boolean = false,
    @SerializedName("likedByMe")
    val likedByMe: Boolean = false,
    @SerializedName("attachment")
    val attachment: Attachment? = null,
    @SerializedName("ownedByMe")
    val ownedByMe: Boolean = false,
    @SerializedName("users")
    val users: Map<Int,UserPreview> = emptyMap(),
)