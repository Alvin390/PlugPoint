package com.PlugPoint.plugpoint.utilis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.PlugPoint.plugpoint.data.ImgurViewModel
import com.PlugPoint.plugpoint.networks.ImgurAPI

class ImgurViewModelFactory(private val imgurAPI: ImgurAPI) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImgurViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ImgurViewModel(imgurAPI) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}