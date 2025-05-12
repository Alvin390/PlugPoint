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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

class ImgurViewModel(private val imgurAPI: ImgurAPI) : ViewModel() {
    private val _uploadState = MutableStateFlow<ImgurUploadState>(ImgurUploadState.Idle)
    val uploadState: StateFlow<ImgurUploadState> get() = _uploadState

    fun uploadImage(uri: Uri?, context: Context, authorization: String) {
        if (uri == null) {
            _uploadState.value = ImgurUploadState.Error("Invalid URI")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uploadState.value = ImgurUploadState.Loading
                val file = ImgurUtils.getFileFromUri(uri, context)
                val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                val multipartBody = MultipartBody.Part.createFormData("image", file.name, requestBody)

                val response = imgurAPI.uploadImage(authorization, multipartBody)
                if (response.isSuccessful && response.body()?.success == true) {
                    _uploadState.value = ImgurUploadState.Success(response.body()!!.data.link)
                } else {
                    _uploadState.value = ImgurUploadState.Error("Failed to upload image")
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