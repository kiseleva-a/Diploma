package ru.netology.nework.utils.listeners

import ru.netology.nework.dto.Coords

interface MapInteractionListener {
    fun onCoordsClick(coords: Coords)
}