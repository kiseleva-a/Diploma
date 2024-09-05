package ru.netology.nework.viewholder

import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.R
import ru.netology.nework.databinding.LayoutJobBinding
import ru.netology.nework.dto.Job
import ru.netology.nework.utils.AndroidUtils.getJobDate
import ru.netology.nework.utils.listeners.JobInteractionListener

class JobViewHolder(
    private val binding: LayoutJobBinding,
    private val onInteractionListener: JobInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(job: Job) {
        binding.apply {
            name.text = job.name
            position.text = job.position
            start.text = getJobDate(job.start)
            if (job.finish != null) {
                finish.text = getJobDate(job.finish)
            } else {
                finish.setText(R.string.this_day)
            }
            if (job.link != null) {
                link.isVisible = true
                link.text = job.link
            } else {
                link.isVisible = false
            }
            menu.isVisible = job.ownedByMe
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(job)
                                true
                            }
                            R.id.edit -> {
                                onInteractionListener.onEdit(job)
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }
        }
    }
}