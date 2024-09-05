package ru.netology.nework.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nework.dto.Coords
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.EventType
import ru.netology.nework.dto.UserPreview

@Entity
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val authorId: Int,
    val author: String,
    val authorAvatar: String?,
    val authorJob: String?,
    val content: String,
    val datetime: String,
    val published: String,
    val coordinatesLat: String?,
    val coordinatesLong: String?,
    val eventType: EventType,
    val likeOwnerIds: List<Int>,
    val likedByMe: Boolean = false,
    val speakersIds: List<Int>,
    val participantIds: List<Int>,
    val participatedByMe: Boolean = false,
    @Embedded
    val attachment: AttachmentEmbedabble?,
    val link: String?,
    val users: Map<Int, UserPreview>,

    val notOnServer: Boolean = false,
    val show: Boolean = true,
) {
    fun toDto() = Event(
        id,
        authorId,
        author,
        authorAvatar,
        authorJob,
        content,
        datetime,
        published,
        if (coordinatesLat == null || coordinatesLong == null) {
            null
        } else {
            Coords(coordinatesLat, coordinatesLong)
        },
        eventType,
        likeOwnerIds,
        likedByMe,
        speakersIds,
        participantIds,
        participatedByMe,
        attachment?.toDto(),
        link,
        false,
        users
    )

    companion object {
        fun fromDto(dto: Event, notOnServer: Boolean = false): EventEntity {
            return EventEntity(
                dto.id,
                dto.authorId,
                dto.author,
                dto.authorAvatar,
                dto.authorJob,
                dto.content,
                dto.datetime,
                dto.published,
                dto.coords?.lat,
                dto.coords?.long,
                dto.type,
                dto.likeOwnerIds,
                dto.likedByMe,
                dto.speakerIds,
                dto.participantsIds,
                dto.participatedByMe,
                AttachmentEmbedabble.fromDto(dto.attachment),
                dto.link,
                dto.users
            )
        }
    }
}