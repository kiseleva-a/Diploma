package ru.netology.nework.dto

import android.net.Uri
import com.google.gson.annotations.SerializedName
import java.io.File

data class MediaModel(val uri: Uri?, val file: File?, val attachmentType: AttachmentType, val url: String?)

data class MediaUpload(val url: String)

data class Attachment(
    @SerializedName("url")
    val url: String,
    @SerializedName("type")
    val type: AttachmentType = AttachmentType.IMAGE
)

enum class AttachmentType {
    IMAGE,
    VIDEO,
    AUDIO,
    NONE,
}
