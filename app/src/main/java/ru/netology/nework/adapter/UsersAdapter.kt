package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import ru.netology.nework.databinding.LayoutUserBinding
import ru.netology.nework.dto.User
import ru.netology.nework.utils.listeners.UserListInteractionListener
import ru.netology.nework.viewholder.UserDiffCallBack
import ru.netology.nework.viewholder.UserViewHolder

class UsersAdapter(
    private val onInteractionListener: UserListInteractionListener
) : ListAdapter<User, UserViewHolder>(UserDiffCallBack()) {
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position) ?: return
        holder.bind(user)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = LayoutUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding, onInteractionListener)
    }

}