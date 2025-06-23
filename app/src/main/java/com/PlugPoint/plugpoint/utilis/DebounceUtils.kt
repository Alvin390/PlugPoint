package com.PlugPoint.plugpoint.utilis

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Lightweight debouncer for events that are not yet Flow-based (e.g. onValueChange callbacks).
 *
 * Example:
 * ```kotlin
 * val debouncer = Debouncer(viewModelScope)
 * fun onQueryChanged(q: String) = debouncer.submit { viewModel.search(q) }
 * ```
 */
class Debouncer(
    private val scope: CoroutineScope,
    private val timeoutMillis: Long = 500L
) {
    private var job: Job? = null
    fun submit(block: () -> Unit) {
        job?.cancel()
        job = scope.launch {
            delay(timeoutMillis)
            block()
        }
    }
}
