package ru.netology.nework.entity

import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.AttachmentType

data class AttachmentEmbedabble(
    val url: String,
    val type: AttachmentType
) {
    fun toDto() = Attachment(url, type)

    companion object {
        fun fromDto(dto: Attachment?) = dto?.let {
            AttachmentEmbedabble(it.url, it.type)
        }
    }
}
