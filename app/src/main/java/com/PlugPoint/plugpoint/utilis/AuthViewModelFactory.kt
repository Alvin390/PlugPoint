package com.PlugPoint.plugpoint.utilis

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.PlugPoint.plugpoint.data.AuthViewModel
import com.PlugPoint.plugpoint.data.ImgurViewModel

class AuthViewModelFactory(
    private val imgurViewModel: ImgurViewModel,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(imgurViewModel, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}