package ru.netology.nework.viewholder

import androidx.recyclerview.widget.DiffUtil
import ru.netology.nework.dto.User

class UserDiffCallBack: DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }
}