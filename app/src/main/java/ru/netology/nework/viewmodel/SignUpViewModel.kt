package ru.netology.nework.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nework.api.ApiService
import ru.netology.nework.auth.AuthPair
import ru.netology.nework.dto.AttachmentType
import ru.netology.nework.dto.MediaModel
import ru.netology.nework.utils.SingleLiveEvent
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {
    private val _signUpError = SingleLiveEvent<String>()
    val signUpError: LiveData<String>
        get() = _signUpError
    private val _signUpRight = SingleLiveEvent<AuthPair>()
    val signUpRight: LiveData<AuthPair>
        get() = _signUpRight

    private val _attachment = MutableLiveData(noMedia)
    val attachment: LiveData<MediaModel>
        get() = _attachment

    fun signUp(login: String, password: String, username: String) = viewModelScope.launch {
        try {
            val response = when (_attachment.value) {
                noMedia -> apiService.registerUser(login, password, username)
                else -> {
                    _attachment.value?.file?.let { file ->
                        val data = MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("login",login)
                            .addFormDataPart("password",password)
                            .addFormDataPart("name",username)
                            .addFormDataPart("file", file.name, file.asRequestBody())
                            .build()
                        apiService.registerUserWithAvatar(data)
                    } ?: throw Exception("Something wrong with sent file")
                }
            }
            if (!response.isSuccessful) {
                Timber.e("Bad response: ${response.message()}")
                _signUpError.postValue(response.message())
            }
            val authPair = response.body() ?: throw RuntimeException("body is null")
            _signUpRight.postValue(authPair)
            Timber.i("Signed up as $login")
        } catch (e: Exception) {
            Timber.e("Error signing up: ${e.message}")
            _signUpError.postValue(e.toString())
        }
    }

    fun changeMedia(fileUri: Uri?, toFile: File?, attachmentType: AttachmentType) {
        _attachment.value = MediaModel(fileUri, toFile, attachmentType, null)
    }

    fun deleteMedia() {
        _attachment.value = noMedia
    }
}

private val noMedia = MediaModel(null, null, AttachmentType.NONE, null)