package ru.netology.nework.dto

sealed interface FeedModelState{
    object Idle : FeedModelState
    object Error : FeedModelState
    object Refreshing : FeedModelState
    object Loading : FeedModelState
}