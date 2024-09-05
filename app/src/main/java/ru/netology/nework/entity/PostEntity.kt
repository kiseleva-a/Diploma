package ru.netology.nework.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nework.dto.Coords
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.UserPreview

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val authorId: Int,
    val author: String,
    val authorAvatar: String?,
    val authorJob: String?,
    val content: String,
    val published: String,
    val coordinatesLat: String?,
    val coordinatesLong: String?,
    val link: String?,
    val likeOwnerIds: List<Int>,
    val mentionIds: List<Int>,
    val mentionedMe: Boolean = false,
    val likedByMe: Boolean = false,
    @Embedded
    val attachment: AttachmentEmbedabble?,
    val users: Map<Int,UserPreview>,

    val notOnServer: Boolean = false,
    val show: Boolean = true,
) {
    fun toDto() = Post(
        id,
        authorId,
        author,
        authorAvatar,
        authorJob,
        content,
        published,
        if (coordinatesLat == null || coordinatesLong == null) {
            null
        } else {
            Coords(coordinatesLat, coordinatesLong)
        },
        link,
        likeOwnerIds,
        mentionIds,
        mentionedMe,
        likedByMe,
        attachment?.toDto(),
        false,
        users
    )

    companion object {
        fun fromDto(dto: Post, notOnServer: Boolean = false): PostEntity {
            return PostEntity(
                dto.id,
                dto.authorId,
                dto.author,
                dto.authorAvatar,
                dto.authorJob,
                dto.content,
                dto.published,
                dto.coords?.lat,
                dto.coords?.long,
                dto.link,
                dto.likeOwnerIds,
                dto.mentionIds,
                dto.mentionedMe,
                dto.likedByMe,
                AttachmentEmbedabble.fromDto(dto.attachment),
                dto.users
            )
        }
    }
}