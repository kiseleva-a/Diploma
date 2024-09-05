package ru.netology.nework.utils.listeners

import ru.netology.nework.dto.Event

interface EventInteractionListener {
    fun onLike(event: Event)
    fun onEdit(event: Event)
    fun onRemove(event: Event)
    fun onAvatarClick(event: Event)
    fun onParticipate(event: Event)
}