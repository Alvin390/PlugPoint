package com.PlugPoint.plugpoint.utilis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DarkModeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DarkModeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DarkModeViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}