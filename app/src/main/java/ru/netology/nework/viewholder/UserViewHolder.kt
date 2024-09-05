package ru.netology.nework.viewholder

import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.R
import ru.netology.nework.databinding.LayoutUserBinding
import ru.netology.nework.dto.User
import ru.netology.nework.utils.listeners.UserListInteractionListener
import ru.netology.nework.utils.loadAvatar

class UserViewHolder(
    private val binding: LayoutUserBinding,
    private val userListInteractionListener: UserListInteractionListener,
) : RecyclerView.ViewHolder(binding.root){

    fun bind(user: User){
        binding.apply {
            userButton.text = user.name
            userButton.isChecked = user.checkedNow
            user.avatar?.let { userButton.loadAvatar(user.avatar) } ?: userButton.setIconResource(R.drawable.baseline_avatar_circle_filled_24)
            userButton.setOnClickListener {
                val checked = userButton.isChecked
                userButton.isChecked = checked
                userListInteractionListener.onClick(user)
            }
        }
    }
}