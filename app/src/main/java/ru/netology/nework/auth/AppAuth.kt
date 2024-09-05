package ru.netology.nework.auth

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class AuthPair(val id: Int, val token: String)

@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext
    private val context: Context,
) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val tokenKey = "TOKEN_KEY"
    private val idKey = "ID_KEY"
    private val _state: MutableStateFlow<AuthState?>

    init {
        val token = prefs.getString(tokenKey, null)
        val id = prefs.getInt(idKey, 0)

        if (token == null || !prefs.contains(idKey)) {
            prefs.edit { clear() }
            _state = MutableStateFlow(null)
        } else {
            _state = MutableStateFlow(AuthState(id, token))
        }
        //sendPushToken()
    }

    val state = _state.asStateFlow()

    fun getToken(): String?{
        return prefs.getString(tokenKey,"")
    }

    fun getId(): Int{
        return prefs.getInt(idKey,0)
    }

    @Synchronized
    fun setAuth(id: Int, token: String) {
        prefs.edit {
            putInt(idKey, id)
            putString(tokenKey, token)
        }
        _state.value = AuthState(id, token)
    }

    @Synchronized
    fun removeAuth() {
        prefs.edit { clear() }
        _state.value = null
    }
}