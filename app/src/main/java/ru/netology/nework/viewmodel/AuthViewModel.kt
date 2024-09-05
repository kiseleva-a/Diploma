package ru.netology.nework.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.netology.nework.auth.AppAuth
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    appAuth: AppAuth,
) : ViewModel() {
    val state = appAuth.state
        .asLiveData()
    val authorized: Boolean
        get() = state.value != null
}