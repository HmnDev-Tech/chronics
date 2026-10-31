package com.hmndev.chronika.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hmndev.chronika.i18n.S
import com.hmndev.chronika.shizuku.ShizukuManager
import com.hmndev.chronika.ui.components.DockDestination
import com.hmndev.chronika.ui.components.FloatingDock
import com.hmndev.chronika.ui.screens.AppPickerScreen
import com.hmndev.chronika.ui.screens.DetailScreen
import com.hmndev.chronika.ui.screens.InsightsScreen
import com.hmndev.chronika.ui.screens.JournalScreen
import com.hmndev.chronika.ui.screens.OnboardingScreen
import com.hmndev.chronika.ui.screens.SettingsScreen
import com.hmndev.chronika.util.PermissionTools
import kotlinx.coroutines.launch

@Composable
fun ChronikaRoot(viewModel: JournalViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val topApps by viewModel.topApps.collectAsStateWithLifecycle()
    val daily by viewModel.daily.collectAsStateWithLifecycle()
    val shizuku by ShizukuManager.state.collectAsStateWithLifecycle()
    val device = rememberDeviceProfile(settings)

    var destination by rememberSaveable { mutableStateOf(DockDestination.JOURNAL.name) }
    var detailId by rememberSaveable { mutableStateOf(-1L) }
    var showPicker by rememberSaveable { mutableStateOf(false) }
    var access by remember { mutableStateOf(PermissionTools.state(context)) }

    fun notify(message: String) {
        if (message.isBlank()) return
        scope.launch { snackbar.showSnackbar(message) }
    }

    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        access = PermissionTools.state(context)
    }

    val backupPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val result = viewModel.importBackup(uri)
                notify(
                    if (result.ok) {
                        S.f("toast.backup_imported", result.imported)
                    } else {
                        S.f("toast.import_failed", result.error)
                    }
                )
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                access = PermissionTools.state(context)
                ShizukuManager.refresh()
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val currentDestination = DockDestination.entries.firstOrNull { it.name == destination }
        ?: DockDestination.JOURNAL
    val detail = entries.firstOrNull { it.id == detailId }

    BackHandler(
        enabled = showPicker || detail != null ||
            (settings.onboardingDone && currentDestination != DockDestination.JOURNAL)
    ) {
        when {
            showPicker -> showPicker = false
            detail != null -> detailId = -1L
            else -> destination = DockDestination.JOURNAL.name
        }
    }

    CompositionLocalProvider(LocalDeviceProfile provides device) {
        when {
            showPicker -> {
                AppPickerScreen(
                    selected = settings.excludedApps,
                    includedOnly = settings.includedOnly,
                    onToggle = { packageName ->
                        viewModel.updateSettings { current ->
                            val next = if (current.excludedApps.contains(packageName)) {
                                current.excludedApps - packageName
                            } else {
                                current.excludedApps + packageName
                            }
                            current.copy(excludedApps = next)
                        }
                    },
                    onSetIncludedOnly = { value ->
                        viewModel.updateSettings { it.copy(includedOnly = value) }
                    },
                    onBack = { showPicker = false }
                )
            }

            detail != null -> {
                DetailScreen(
                    entry = detail,
                    showRawByDefault = settings.showRawPayload,
                    onBack = { detailId = -1L },
                    onPin = { viewModel.togglePin(detail) },
                    onToggleRead = { viewModel.markRead(detail, !detail.isRead) },
                    onArchive = {
                        viewModel.archive(detail, !detail.isArchived)
                        detailId = -1L
                        notify(if (detail.isArchived) S["toast.restored"] else S["toast.archived"])
                    },
                    onDelete = {
                        viewModel.delete(detail)
                        detailId = -1L
                        notify(S["toast.deleted"])
                    },
                    onExport = {
                        scope.launch {
                            val result = viewModel.export(detail)
                            notify(
                                if (result.ok) {
                                    S.f("toast.saved", settings.safeFolderName)
                                } else {
                                    S.f("toast.save_failed", result.error)
                                }
                            )
                        }
                    },
                    onCopied = { notify(S["toast.copied"]) }
                )
            }

            !settings.onboardingDone -> {
                OnboardingScreen(
                    access = access,
                    shizuku = shizuku,
                    onOpenNotificationSettings = { PermissionTools.openNotificationSettings(context) },
                    onOpenAccessibilitySettings = { PermissionTools.openAccessibilitySettings(context) },
                    onOpenShizuku = { PermissionTools.openShizuku(context) },
                    onRequestShizuku = { ShizukuManager.requestPermission() },
                    onGrantListener = {
                        scope.launch {
                            notify(viewModel.grantListener())
                            access = PermissionTools.state(context)
                        }
                    },
                    onGrantAccessibility = {
                        scope.launch {
                            notify(viewModel.grantAccessibility())
                            access = PermissionTools.state(context)
                        }
                    },
                    onRequestPostNotifications = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            access = PermissionTools.state(context)
                        }
                    },
                    onTest = {
                        if (viewModel.testNotification()) {
                            notify(S["toast.test_sent"])
                        } else {
                            notify(S["toast.test_failed"])
                        }
                    },
                    onFinish = {
                        viewModel.updateSettings { it.copy(onboardingDone = true) }
                        destination = DockDestination.JOURNAL.name
                    }
                )
            }

            else -> {
                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    snackbarHost = { SnackbarHost(snackbar) }
                ) { padding ->
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        AnimatedContent(
                            targetState = currentDestination,
                            modifier = Modifier
                                .fillMaxHeight()
                                .then(
                                    if (device.screenClass == ScreenClass.EXPANDED) {
                                        Modifier.widthIn(max = device.contentMaxWidth).fillMaxWidth()
                                    } else {
                                        Modifier.fillMaxWidth()
                                    }
                                ),
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "screens"
                        ) { target ->
                            when (target) {
                                DockDestination.JOURNAL -> JournalScreen(
                                    entries = entries,
                                    stats = stats,
                                    settings = settings,
                                    access = access,
                                    onOpenEntry = { entry ->
                                        if (!entry.isRead) viewModel.markRead(entry, true)
                                        detailId = entry.id
                                    },
                                    onPin = { viewModel.togglePin(it) },
                                    onMarkAllRead = {
                                        viewModel.markAllRead()
                                        notify(S["toast.marked_read"])
                                    },
                                    onOpenSettings = { destination = DockDestination.SETTINGS.name },
                                    onTest = {
                                        if (viewModel.testNotification()) {
                                            notify(S["toast.test_sent_short"])
                                        } else {
                                            notify(S["toast.test_failed_short"])
                                        }
                                    },
                                    onOpenInsights = { destination = DockDestination.INSIGHTS.name }
                                )

                                DockDestination.INSIGHTS -> InsightsScreen(
                                    stats = stats,
                                    topApps = topApps,
                                    daily = daily,
                                    entries = entries,
                                    settings = settings
                                )

                                DockDestination.SETTINGS -> SettingsScreen(
                                    settings = settings,
                                    access = access,
                                    onUpdate = { transform -> viewModel.updateSettings(transform) },
                                    onOpenAppPicker = { showPicker = true },
                                    onOpenNotificationSettings = {
                                        PermissionTools.openNotificationSettings(context)
                                    },
                                    onOpenAccessibilitySettings = {
                                        PermissionTools.openAccessibilitySettings(context)
                                    },
                                    onOpenShizuku = { PermissionTools.openShizuku(context) },
                                    onRequestShizuku = { ShizukuManager.requestPermission() },
                                    onGrantListener = {
                                        scope.launch {
                                            notify(viewModel.grantListener())
                                            access = PermissionTools.state(context)
                                        }
                                    },
                                    onGrantAccessibility = {
                                        scope.launch {
                                            notify(viewModel.grantAccessibility())
                                            access = PermissionTools.state(context)
                                        }
                                    },
                                    onImportShizuku = {
                                        scope.launch {
                                            val result = viewModel.importFromShizuku()
                                            notify(result.message)
                                            viewModel.refresh()
                                        }
                                    },
                                    onTest = {
                                        if (viewModel.testNotification()) {
                                            notify(S["toast.test_sent_short"])
                                        } else {
                                            notify(S["toast.test_failed_short"])
                                        }
                                    },
                                    onExportAll = {
                                        scope.launch {
                                            val result = viewModel.exportAll()
                                            val folder = settings.safeFolderName
                                            notify(
                                                if (result.ok) {
                                                    S.f("toast.backup_saved", folder)
                                                } else {
                                                    S.f("toast.export_failed", result.error)
                                                }
                                            )
                                        }
                                    },
                                    onImportBackup = {
                                        backupPicker.launch(arrayOf("application/json", "*/*"))
                                    },
                                    onDeleteAll = {
                                        viewModel.deleteAll()
                                        notify(S["toast.cleared"])
                                    },
                                    onOpenUrl = { url -> PermissionTools.openUrl(context, url) }
                                )
                            }
                        }

                        FloatingDock(
                            selected = currentDestination,
                            onSelect = { destination = it.name },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding()
                                .padding(bottom = device.dockBottomPadding)
                        )
                    }
                }
            }
        }
    }
}
