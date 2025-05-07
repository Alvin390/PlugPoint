package com.PlugPoint.plugpoint.data

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.PlugPoint.plugpoint.utilis.DarkModePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DarkModeViewModel(private val context: Context) : ViewModel() {
    private val _isDarkModeEnabled = MutableStateFlow(false)
    val isDarkModeEnabled: StateFlow<Boolean> get() = _isDarkModeEnabled

    init {
        viewModelScope.launch {
            _isDarkModeEnabled.value = DarkModePreferences.getDarkModeState(context).first()
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            _isDarkModeEnabled.value = enabled
            DarkModePreferences.saveDarkModeState(context, enabled)
        }
    }
}
