package ru.netology.nework.viewholder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.R
import ru.netology.nework.databinding.LayoutEventBinding
import ru.netology.nework.dto.AttachmentType
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.EventType
import ru.netology.nework.dto.UserPreview
import ru.netology.nework.utils.AndroidUtils.formattingBigNumbers
import ru.netology.nework.utils.listeners.EventInteractionListener
import ru.netology.nework.utils.listeners.MapInteractionListener
import ru.netology.nework.utils.listeners.MediaInteractionListener
import ru.netology.nework.utils.load
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class EventViewHolder(
    private val binding: LayoutEventBinding,
    private val onInteractionListener: EventInteractionListener,
    private val mediaInteractionListener: MediaInteractionListener,
    private val mapInteractionListener: MapInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(event: Event) {
        binding.apply {
            author.text = event.author
            if (event.authorAvatar != null) {
                avatar.load(event.authorAvatar, true)
            } else {
                avatar.setImageResource(R.drawable.baseline_person_24)
            }
            avatar.setOnClickListener {
                onInteractionListener.onAvatarClick(event)
            }
            try {
                val publishedTime = OffsetDateTime.parse(event.published).toLocalDateTime()
                val formatter = DateTimeFormatter.ofPattern("HH:mm:ss yyyy-MM-dd")
                published.text = publishedTime.format(formatter)
            } catch (e: Exception){
                println(e.message)
                published.setText(R.string.posted_now)
            }

            val eventDateTime = OffsetDateTime.parse(event.datetime).toLocalDateTime()
            val formatter = DateTimeFormatter.ofPattern("HH:mm yyyy-MM-dd")
            eventTime.text = eventDateTime.format(formatter)

            eventType.text = if(event.type == EventType.OFFLINE)"OFFLINE" else "ONLINE"

            content.text = event.content
            ifHaveTextThenShow(job,event.authorJob)
            ifHaveTextThenShow(link, event.link)
            ifHaveTextThenShow(coordinates, event.coords)
            if(coordinates.isVisible){
                coordinates.setOnClickListener { mapInteractionListener.onCoordsClick(event.coords!!) }
            }

            like.text = formattingBigNumbers(event.likeOwnerIds.size)
            like.isChecked = event.likedByMe
            like.setOnClickListener { onInteractionListener.onLike(event) }
            showAvatarInTrailing(event.likeOwnerIds,0,likeAvatars1,event.users)
            showAvatarInTrailing(event.likeOwnerIds,1,likeAvatars2,event.users)
            showAvatarInTrailing(event.likeOwnerIds,2,likeAvatars3,event.users)
            likeBatchTrail.isVisible = event.likeOwnerIds.size > 3

            speaker.text = formattingBigNumbers(event.speakerIds.size)
            showAvatarInTrailing(event.speakerIds,0,speakerAvatars1,event.users)
            showAvatarInTrailing(event.speakerIds,1,speakerAvatars2,event.users)
            showAvatarInTrailing(event.speakerIds,2,speakerAvatars3,event.users)
            speakerBatchTrail.isVisible = event.speakerIds.size > 3

            participant.isChecked = event.participatedByMe
            participant.setOnClickListener { onInteractionListener.onParticipate(event) }
            participant.text = formattingBigNumbers(event.participantsIds.size)
            showAvatarInTrailing(event.participantsIds,0,participantAvatars1,event.users)
            showAvatarInTrailing(event.participantsIds,1,participantAvatars2,event.users)
            showAvatarInTrailing(event.participantsIds,2,participantAvatars3,event.users)
            participantBatchTrail.isVisible = event.participantsIds.size > 3

            if (event.attachment != null) {
                val attachmentUrl = event.attachment.url
                when (event.attachment.type) {
                    AttachmentType.IMAGE -> {
                        attachmentPicture.visibility = View.VISIBLE
                        playAttachment.visibility = View.GONE
                        attachmentPicture.load(attachmentUrl)
                        attachmentPicture.setOnClickListener {
                            mediaInteractionListener.onPictureClick(attachmentUrl)
                        }
                    }
                    AttachmentType.VIDEO -> {
                        attachmentPicture.visibility = View.GONE
                        playAttachment.visibility = View.VISIBLE
                        playAttachment.setImageResource(R.drawable.baseline_video_48)
                        playAttachment.setOnClickListener {
                            mediaInteractionListener.onVideoClick(attachmentUrl)
                        }
                    }
                    AttachmentType.AUDIO -> {
                        attachmentPicture.visibility = View.GONE
                        playAttachment.visibility = View.VISIBLE
                        playAttachment.setImageResource(R.drawable.baseline_audio_file_48)
                        playAttachment.setOnClickListener {
                            mediaInteractionListener.onAudioClick(attachmentUrl)
                        }
                    }
                    else -> {}
                }
            } else {
                attachmentPicture.visibility = View.GONE
                playAttachment.visibility = View.GONE
            }

            if (event.author == "Me") {
                notOnServer.visibility = View.VISIBLE
                bottomGroup.visibility = View.GONE
            } else {
                notOnServer.visibility = View.GONE
                bottomGroup.visibility = View.VISIBLE
            }

            menu.isVisible = event.ownedByMe
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(event)
                                true
                            }
                            R.id.edit -> {
                                onInteractionListener.onEdit(event)
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }
        }
    }

    private fun showAvatarInTrailing(
        avatarUserIdList: List<Int>,
        num: Int,
        avatar: ImageView,
        users: Map<Int, UserPreview>
    ) {
        if (avatarUserIdList.size > num) {
            val userId = avatarUserIdList[num]
            val user = users[userId]
            avatar.isVisible = true
            if (user != null && user.avatar != null) {
                avatar.load(user.avatar, true)
            } else {
                avatar.setImageResource(R.drawable.baseline_avatar_circle_filled_24)
            }
        } else {
            avatar.isVisible = false
        }
    }


    private fun <T> ifHaveTextThenShow(view: TextView, param: T?) {
        if (param != null) {
            view.isVisible = true
            view.text = param.toString()
        } else {
            view.isVisible = false
        }
    }
}