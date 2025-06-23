package com.PlugPoint.plugpoint.data



import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.PlugPoint.plugpoint.networks.ImgurAPI
import com.PlugPoint.plugpoint.utilis.ImgurUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.PlugPoint.plugpoint.ui.state.UiStateManager
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

class ImgurViewModel(private val imgurAPI: ImgurAPI) : ViewModel() {
    companion object {
        /** Replace with your real Imgur client id and keep it secure (e.g. via remote config). */
        val AUTHORIZATION: String get() = "Client-ID ${com.PlugPoint.plugpoint.BuildConfig.IMGUR_CLIENT_ID}"
    }
    private val _uploadState = MutableStateFlow<ImgurUploadState>(ImgurUploadState.Idle)
    val uploadState: StateFlow<ImgurUploadState> get() = _uploadState

    /**
     * Suspend-friendly upload that returns the image URL or throws an exception.
     * This avoids having to wait for StateFlow externally and prevents race conditions.
     */
    suspend fun uploadImageAndGetUrl(uri: Uri, context: Context, authorization: String): String {
        return withContext(Dispatchers.IO) {
            val file = ImgurUtils.getFileFromUri(uri, context)
            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData("image", file.name, requestBody)

            UiStateManager.setLoading()
            val response = imgurAPI.uploadImage(authorization, multipartBody)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()!!.data.link.also { UiStateManager.setIdle() }
            } else {
                UiStateManager.setError("Failed to upload image")
                throw Exception("Failed to upload image")
            }
        }
    }

    /**
     * Legacy method kept for screens that observe [uploadState]. Internally delegates
     * to [uploadImageAndGetUrl] so behaviour stays consistent.
     */
    fun uploadImage(uri: Uri?, context: Context, authorization: String) {
        if (uri == null) {
            _uploadState.value = ImgurUploadState.Error("Invalid URI")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uploadState.value = ImgurUploadState.Loading
                try {
                    val link = uploadImageAndGetUrl(uri, context, authorization)
                    _uploadState.value = ImgurUploadState.Success(link)
                } catch (e: Exception) {
                    _uploadState.value = ImgurUploadState.Error(e.message ?: "Failed to upload image")
                }
            } catch (e: Exception) {
                _uploadState.value = ImgurUploadState.Error("Failed to upload image. Please try again.")
            }
        }
    }
}
sealed class ImgurUploadState {
    object Idle : ImgurUploadState()
    object Loading : ImgurUploadState()
    data class Success(val imageUrl: String) : ImgurUploadState()
    data class Error(val message: String) : ImgurUploadState()
}