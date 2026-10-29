package com.hmndev.chronika.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hmndev.chronika.data.AppCount
import com.hmndev.chronika.data.AppSettings
import com.hmndev.chronika.data.DayCount
import com.hmndev.chronika.data.JournalEntry
import com.hmndev.chronika.data.JournalRepository
import com.hmndev.chronika.data.JournalStats
import com.hmndev.chronika.data.JournalStore
import com.hmndev.chronika.data.SettingsStore
import com.hmndev.chronika.i18n.S
import com.hmndev.chronika.service.CaptureEngine
import com.hmndev.chronika.shizuku.ImportResult
import com.hmndev.chronika.shizuku.ShizukuManager
import com.hmndev.chronika.util.ExportManager
import com.hmndev.chronika.util.ExportResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class JournalViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = JournalRepository(application)

    val settings: StateFlow<AppSettings> = SettingsStore.settings

    private val _entries = MutableStateFlow<List<JournalEntry>>(emptyList())
    val entries: StateFlow<List<JournalEntry>> = _entries.asStateFlow()

    private val _stats = MutableStateFlow(JournalStats())
    val stats: StateFlow<JournalStats> = _stats.asStateFlow()

    private val _topApps = MutableStateFlow<List<AppCount>>(emptyList())
    val topApps: StateFlow<List<AppCount>> = _topApps.asStateFlow()

    private val _daily = MutableStateFlow<List<DayCount>>(emptyList())
    val daily: StateFlow<List<DayCount>> = _daily.asStateFlow()

    init {
        refresh()
        viewModelScope.launch {
            JournalStore.version.collect {
                refresh()
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val data = withContext(Dispatchers.IO) {
                Bundle(
                    repo.entries(),
                    repo.stats(),
                    repo.topApps(),
                    repo.dailyCounts()
                )
            }
            _entries.value = data.entries
            _stats.value = data.stats
            _topApps.value = data.apps
            _daily.value = data.daily
        }
    }

    fun updateSettings(transform: (AppSettings) -> AppSettings) {
        SettingsStore.update(transform)
    }

    fun markRead(entry: JournalEntry, read: Boolean = true) {
        viewModelScope.launch(Dispatchers.IO) { repo.markRead(entry.id, read) }
    }

    fun togglePin(entry: JournalEntry) {
        viewModelScope.launch(Dispatchers.IO) { repo.togglePinned(entry) }
    }

    fun archive(entry: JournalEntry, archived: Boolean) {
        viewModelScope.launch(Dispatchers.IO) { repo.setArchived(entry, archived) }
    }

    fun delete(entry: JournalEntry) {
        viewModelScope.launch(Dispatchers.IO) { repo.delete(entry.id) }
    }

    fun deleteAll() {
        viewModelScope.launch(Dispatchers.IO) {
            repo.deleteAll()
            JournalStore.changed()
        }
    }

    suspend fun export(entry: JournalEntry): ExportResult {
        val result = ExportManager.export(getApplication(), entry, settings.value)
        withContext(Dispatchers.IO) {
            if (result.imagePaths.isNotEmpty()) {
                repo.setImagePaths(
                    entry.id,
                    org.json.JSONArray().apply { result.imagePaths.forEach { put(it) } }.toString()
                )
            }
            if (result.jsonPath.isNotBlank() || result.txtPath.isNotBlank()) {
                repo.setExportPath(entry.id, result.jsonPath.ifBlank { result.txtPath })
            }
        }
        return result
    }

    suspend fun exportAll(): ExportResult =
        ExportManager.exportAll(getApplication(), entries.value, settings.value)

    fun testNotification(): Boolean =
        CaptureEngine.postTestNotification(getApplication())

    suspend fun importFromShizuku(): ImportResult =
        ShizukuManager.importActiveNotifications(getApplication())

    suspend fun grantListener(): String {
        val result = ShizukuManager.grantNotificationListener(getApplication())
        return result.message.ifBlank { if (result.ok) S["common.done"] else S["common.failed"] }
    }

    suspend fun grantAccessibility(): String {
        val result = ShizukuManager.grantAccessibility(getApplication())
        return result.message.ifBlank { if (result.ok) S["common.done"] else S["common.failed"] }
    }

    private data class Bundle(
        val entries: List<JournalEntry>,
        val stats: JournalStats,
        val apps: List<AppCount>,
        val daily: List<DayCount>
    )
}
