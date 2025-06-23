package com.PlugPoint.plugpoint.ui.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Simple sealed hierarchy representing generic UI status across the app.
 */
sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Error(val message: String) : UiState()
}

/**
 * Singleton holder that any ViewModel can interact with to publish global UI state changes.
 * This avoids having to plumb a reference to a central ViewModel through every feature
 * and is lifecycle-safe because it only contains process-wide flows (no references to views).
 */
object UiStateManager {
    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    fun setLoading() {
        _uiState.value = UiState.Loading
    }

    fun setIdle() {
        _uiState.value = UiState.Idle
    }

    fun setError(message: String) {
        _uiState.value = UiState.Error(message)
    }
}
