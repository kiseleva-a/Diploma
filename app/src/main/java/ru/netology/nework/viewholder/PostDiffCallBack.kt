package ru.netology.nework.viewholder

import androidx.recyclerview.widget.DiffUtil
import ru.netology.nework.dto.Post

class PostDiffCallBack: DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}