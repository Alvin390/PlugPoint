package com.PlugPoint.plugpoint.utilis

import CommodityViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.PlugPoint.plugpoint.data.ImgurViewModel

class CommoditiesViewModelFactory(
    private val imgurViewModel: ImgurViewModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CommodityViewModel::class.java)) {
            return CommodityViewModel(imgurViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}