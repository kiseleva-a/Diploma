package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import ru.netology.nework.databinding.LayoutEventBinding
import ru.netology.nework.dto.Event
import ru.netology.nework.utils.listeners.EventInteractionListener
import ru.netology.nework.utils.listeners.MapInteractionListener
import ru.netology.nework.utils.listeners.MediaInteractionListener
import ru.netology.nework.viewholder.EventDiffCallBack
import ru.netology.nework.viewholder.EventViewHolder

class EventAdapter(
    private val onInteractionListener: EventInteractionListener,
    private val mediaInteractionListener: MediaInteractionListener,
    private val mapInteractionListener: MapInteractionListener,
) : PagingDataAdapter<Event, EventViewHolder>(EventDiffCallBack()) {
    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = getItem(position) ?: return
        holder.bind(event)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = LayoutEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding, onInteractionListener, mediaInteractionListener, mapInteractionListener)
    }

}