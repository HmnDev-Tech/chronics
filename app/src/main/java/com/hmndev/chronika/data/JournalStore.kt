package com.hmndev.chronika.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-process change signal. The notification listener, accessibility service and the UI
 * live in the same process, so a simple version counter is enough to refresh the journal.
 */
object JournalStore {
    private val _version = MutableStateFlow(0L)
    val version: StateFlow<Long> = _version.asStateFlow()

    fun changed() {
        _version.value = _version.value + 1
    }
}
