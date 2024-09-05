package ru.netology.nework.utils.listeners

interface MediaInteractionListener {
    fun onAudioClick(url: String)
    fun onVideoClick(url: String)
    fun onPictureClick(url: String)
}