package com.PlugPoint.plugpoint.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.PlugPoint.plugpoint.ui.state.UiState
import com.PlugPoint.plugpoint.ui.state.UiStateManager

@Composable
fun rememberSnackbarHostState(): SnackbarHostState = remember { SnackbarHostState() }

/** Shows a full-screen loader or snackbar based on [UiStateManager]. */
@Composable
fun UiStateOverlay(snackbarHostState: SnackbarHostState = rememberSnackbarHostState()) {
    val state by UiStateManager.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (state is UiState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        SnackbarHost(hostState = snackbarHostState)
    }

    LaunchedEffect(state) {
        if (state is UiState.Error) {
            snackbarHostState.showSnackbar((state as UiState.Error).message)
            UiStateManager.setIdle()
        }
    }
}
