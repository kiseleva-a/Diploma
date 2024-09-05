package ru.netology.nework.viewholder

import androidx.recyclerview.widget.DiffUtil
import ru.netology.nework.dto.Job

class JobDiffCallBack: DiffUtil.ItemCallback<Job>() {
    override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean {
        return oldItem == newItem
    }
}