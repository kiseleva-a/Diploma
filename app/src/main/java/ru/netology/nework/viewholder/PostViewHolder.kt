package ru.netology.nework.viewholder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.R
import ru.netology.nework.databinding.LayoutPostBinding
import ru.netology.nework.dto.AttachmentType
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.UserPreview
import ru.netology.nework.utils.AndroidUtils.formattingBigNumbers
import ru.netology.nework.utils.listeners.MapInteractionListener
import ru.netology.nework.utils.listeners.MediaInteractionListener
import ru.netology.nework.utils.listeners.PostInteractionListener
import ru.netology.nework.utils.load
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class PostViewHolder(
    private val binding: LayoutPostBinding,
    private val onInteractionListener: PostInteractionListener,
    private val mediaInteractionListener: MediaInteractionListener,
    private val mapInteractionListener: MapInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            if (post.authorAvatar != null) {
                avatar.load(post.authorAvatar, true)
            } else {
                avatar.setImageResource(R.drawable.baseline_person_24)
            }
            avatar.setOnClickListener {
                onInteractionListener.onAvatarClick(post)
            }
            try {
                val publishedTime = OffsetDateTime.parse(post.published).toLocalDateTime()
                val formatter = DateTimeFormatter.ofPattern("HH:mm:ss yyyy-MM-dd")
                published.text = publishedTime.format(formatter)
            } catch (e: Exception){
                println(e.message)
                published.setText(R.string.posted_now)
            }
            content.text = post.content
            ifHaveTextThenShow(job,post.authorJob)
            ifHaveTextThenShow(link, post.link)
            ifHaveTextThenShow(coordinates, post.coords)
            if(coordinates.isVisible){
                coordinates.setOnClickListener { mapInteractionListener.onCoordsClick(post.coords!!) }
            }

            like.text = formattingBigNumbers(post.likeOwnerIds.size)
            like.isChecked = post.likedByMe
            like.setOnClickListener { onInteractionListener.onLike(post) }

            showAvatarInTrailing(post.likeOwnerIds,0,likeAvatars1,post.users)
            showAvatarInTrailing(post.likeOwnerIds,1,likeAvatars2,post.users)
            showAvatarInTrailing(post.likeOwnerIds,2,likeAvatars3,post.users)
            likeBatchTrail.isVisible = post.likeOwnerIds.size > 3

            mention.text = formattingBigNumbers(post.mentionIds.size)
            showAvatarInTrailing(post.mentionIds,0,mentionAvatars1,post.users)
            showAvatarInTrailing(post.mentionIds,1,mentionAvatars2,post.users)
            showAvatarInTrailing(post.mentionIds,2,mentionAvatars3,post.users)
            mentionBatchTrail.isVisible = post.mentionIds.size > 3

            if (post.attachment != null) {
                val attachmentUrl = post.attachment.url
                when (post.attachment.type) {
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

            if (post.author == "Me") {
                notOnServer.visibility = View.VISIBLE
                bottomGroup.visibility = View.GONE
            } else {
                notOnServer.visibility = View.GONE
                bottomGroup.visibility = View.VISIBLE
            }

            menu.isVisible = post.ownedByMe
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }
                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
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