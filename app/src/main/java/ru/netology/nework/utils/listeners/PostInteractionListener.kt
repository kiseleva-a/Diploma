package ru.netology.nework.utils.listeners

import ru.netology.nework.dto.Post

interface PostInteractionListener {
    fun onLike(post: Post)
    fun onEdit(post: Post)
    fun onRemove(post: Post)
    fun onAvatarClick(post: Post)
}