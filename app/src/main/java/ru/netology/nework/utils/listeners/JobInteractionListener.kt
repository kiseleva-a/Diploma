package ru.netology.nework.utils.listeners

import ru.netology.nework.dto.Job

interface JobInteractionListener {
    fun onEdit(job: Job)
    fun onRemove(job: Job)
}