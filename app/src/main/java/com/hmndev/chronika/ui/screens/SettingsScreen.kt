package com.hmndev.chronika.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Accessibility
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.RestorePage
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hmndev.chronika.BuildConfig
import com.hmndev.chronika.data.AppSettings
import com.hmndev.chronika.i18n.AppLanguage
import com.hmndev.chronika.i18n.S
import com.hmndev.chronika.service.CaptureEngine
import com.hmndev.chronika.shizuku.ShizukuManager
import com.hmndev.chronika.ui.LocalDeviceProfile
import com.hmndev.chronika.ui.ScreenClass
import com.hmndev.chronika.ui.components.SettingAction
import com.hmndev.chronika.ui.components.SettingIcon
import com.hmndev.chronika.ui.components.SettingSwitch
import com.hmndev.chronika.ui.components.SettingValueRow
import com.hmndev.chronika.ui.components.SettingsGroupDivider
import com.hmndev.chronika.ui.components.SettingsSection
import com.hmndev.chronika.ui.theme.Coral
import com.hmndev.chronika.ui.theme.Lime
import com.hmndev.chronika.util.AccessState
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    settings: AppSettings,
    access: AccessState,
    onUpdate: ((AppSettings) -> AppSettings) -> Unit,
    onOpenAppPicker: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenShizuku: () -> Unit,
    onRequestShizuku: () -> Unit,
    onGrantListener: () -> Unit,
    onGrantAccessibility: () -> Unit,
    onImportShizuku: () -> Unit,
    onTest: () -> Unit,
    onExportAll: () -> Unit,
    onImportBackup: () -> Unit = {},
    onDeleteAll: () -> Unit,
    onOpenUrl: (String) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val device = LocalDeviceProfile.current
    val context = LocalContext.current
    val currentLanguage = AppLanguage.resolve(settings.language)
    val shizuku by ShizukuManager.state.collectAsState()
    var showDelete by remember { mutableStateOf(false) }
    var retentionDraft by remember(settings.retentionDays) {
        mutableStateOf(settings.retentionDays.toFloat())
    }
    var maxEntriesDraft by remember(settings.maxEntries) {
        mutableStateOf(settings.maxEntries.toString())
    }
    var scaleDraft by remember(settings.uiScale) {
        mutableStateOf(settings.uiScale)
    }
    val focusManager = LocalFocusManager.current

    fun commitMaxEntries() {
        val parsed = maxEntriesDraft.toIntOrNull()?.coerceIn(500, 200000)
            ?: settings.maxEntries
        maxEntriesDraft = parsed.toString()
        if (parsed != settings.maxEntries) {
            onUpdate { it.copy(maxEntries = parsed) }
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(
            start = device.screenPadding,
            end = device.screenPadding,
            top = device.dp(14.dp),
            bottom = device.dp(126.dp)
        ),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item(key = "heading") {
            Column {
                Text(
                    text = S["settings.eyebrow"],
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.primary
                )
                Text(
                    text = S["settings.title"],
                    style = MaterialTheme.typography.displaySmall,
                    color = scheme.onSurface
                )
                Text(
                    text = S["settings.subtitle"],
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
            }
        }

        item(key = "access") {
            SettingsSection(
                title = S["settings.access"],
                subtitle = S["settings.access_sub"],
                accent = Coral
            ) {
                AccessRow(
                    title = S["settings.listener"],
                    description = S["settings.listener_desc"],
                    granted = access.listener,
                    onClick = onOpenNotificationSettings
                )
                SettingsGroupDivider()
                AccessRow(
                    title = S["settings.accessibility"],
                    description = S["settings.accessibility_desc"],
                    granted = access.accessibility,
                    onClick = onOpenAccessibilitySettings
                )
                SettingsGroupDivider()
                AccessRow(
                    title = "Shizuku",
                    description = shizuku.message,
                    granted = shizuku.available && shizuku.permissionGranted,
                    onClick = onOpenShizuku
                )
                if (shizuku.available && !shizuku.permissionGranted) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(onClick = onRequestShizuku, shape = CircleShape) {
                            Text(S["settings.shizuku_allow"])
                        }
                    }
                }
                if (shizuku.permissionGranted) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(onClick = onGrantListener, shape = CircleShape) {
                            Text(S["settings.grant_listener"])
                        }
                        FilledTonalButton(onClick = onGrantAccessibility, shape = CircleShape) {
                            Text(S["settings.grant_access"])
                        }
                    }
                    SettingAction(
                        title = S["settings.import_active"],
                        description = S["settings.import_active_desc"],
                        icon = Icons.Rounded.CloudDownload,
                        onClick = onImportShizuku
                    )
                }
                SettingAction(
                    title = S["settings.open_shizuku"],
                    description = S["settings.open_shizuku_desc"],
                    icon = Icons.Rounded.Terminal,
                    onClick = onOpenShizuku
                )
            }
        }

        item(key = "capture") {
            SettingsSection(
                title = S["settings.capture"],
                subtitle = S["settings.capture_sub"],
                accent = Lime
            ) {
                SettingSwitch(
                    title = S["settings.capture_enabled"],
                    description = S["settings.capture_enabled_desc"],
                    checked = settings.captureEnabled,
                    onCheckedChange = { value -> onUpdate { it.copy(captureEnabled = value) } },
                    icon = Icons.Rounded.NotificationsActive
                )
                SettingsGroupDivider()
                SettingSwitch(
                    title = S["settings.listener"],
                    description = S["settings.listener_desc2"],
                    checked = settings.captureListener,
                    onCheckedChange = { value -> onUpdate { it.copy(captureListener = value) } },
                    icon = Icons.Rounded.Bolt,
                    enabled = settings.captureEnabled
                )
                SettingsGroupDivider()
                SettingSwitch(
                    title = S["settings.accessibility"],
                    description = S["settings.accessibility_desc2"],
                    checked = settings.captureAccessibility,
                    onCheckedChange = { value -> onUpdate { it.copy(captureAccessibility = value) } },
                    icon = Icons.Rounded.Accessibility,
                    enabled = settings.captureEnabled
                )
                SettingsGroupDivider()
                SettingSwitch(
                    title = S["settings.deep"],
                    description = S["settings.deep_desc"],
                    checked = settings.deepAccessibility,
                    onCheckedChange = { value -> onUpdate { it.copy(deepAccessibility = value) } },
                    icon = Icons.Rounded.Tune,
                    enabled = settings.captureEnabled && settings.captureAccessibility
                )
                SettingsGroupDivider()
                SettingSwitch(
                    title = S["settings.autoexport"],
                    description = S["settings.autoexport_desc"],
                    checked = settings.autoExport,
                    onCheckedChange = { value -> onUpdate { it.copy(autoExport = value) } },
                    icon = Icons.Rounded.Download
                )
                SettingsGroupDivider()
                SettingSwitch(
                    title = S["settings.images"],
                    description = S["settings.images_desc"],
                    checked = settings.saveImages,
                    onCheckedChange = { value -> onUpdate { it.copy(saveImages = value) } },
                    icon = Icons.Rounded.ColorLens
                )
                SettingsGroupDivider()
                SettingSwitch(
                    title = S["settings.raw_extras"],
                    description = S["settings.raw_extras_desc"],
                    checked = settings.saveRawExtras,
                    onCheckedChange = { value -> onUpdate { it.copy(saveRawExtras = value) } },
                    icon = Icons.Rounded.Storage
                )
                SettingsGroupDivider()
                SettingSwitch(
                    title = S["settings.dedupe"],
                    description = S["settings.dedupe_desc"],
                    checked = settings.dedupe,
                    onCheckedChange = { value -> onUpdate { it.copy(dedupe = value) } },
                    icon = Icons.Rounded.AutoAwesome
                )
                SettingsGroupDivider()
                SettingSwitch(
                    title = S["settings.ignore_ongoing"],
                    description = S["settings.ignore_ongoing_desc"],
                    checked = settings.ignoreOngoing,
                    onCheckedChange = { value -> onUpdate { it.copy(ignoreOngoing = value) } },
                    icon = Icons.Rounded.CleaningServices
                )
                SettingsGroupDivider()
                SettingSwitch(
                    title = S["settings.ignore_silent"],
                    description = S["settings.ignore_silent_desc"],
                    checked = settings.ignoreSilent,
                    onCheckedChange = { value -> onUpdate { it.copy(ignoreSilent = value) } },
                    icon = Icons.Rounded.Bolt
                )
            }
        }

        item(key = "filters") {
            SettingsSection(
                title = S["settings.filters"],
                subtitle = S["settings.filters_sub"],
                accent = scheme.tertiary
            ) {
                SettingAction(
                    title = if (settings.includedOnly) S["settings.only_selected"] else S["settings.excluded"],
                    description = if (settings.excludedApps.isEmpty()) {
                        S["settings.list_empty"]
                    } else {
                        S.f("settings.selected_count", settings.excludedApps.size)
                    },
                    icon = Icons.Rounded.Apps,
                    onClick = onOpenAppPicker,
                    trailing = settings.excludedApps.size.toString()
                )
                SettingsGroupDivider()
                SettingSwitch(
                    title = S["settings.whitelist"],
                    description = S["settings.whitelist_desc"],
                    checked = settings.includedOnly,
                    onCheckedChange = { value -> onUpdate { it.copy(includedOnly = value) } },
                    icon = Icons.Rounded.Security
                )
                SettingsGroupDivider()
                OutlinedTextField(
                    value = settings.excludedKeywords,
                    onValueChange = { value -> onUpdate { it.copy(excludedKeywords = value) } },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    label = { Text(S["settings.stop_words"]) },
                    placeholder = { Text(S["settings.stop_hint"]) },
                    shape = MaterialTheme.shapes.medium,
                    minLines = 1,
                    maxLines = 3
                )
                OutlinedTextField(
                    value = settings.requiredKeywords,
                    onValueChange = { value -> onUpdate { it.copy(requiredKeywords = value) } },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    label = { Text(S["settings.required_words"]) },
                    placeholder = { Text(S["settings.required_hint"]) },
                    shape = MaterialTheme.shapes.medium,
                    minLines = 1,
                    maxLines = 3
                )
                SettingsGroupDivider()
                SettingSwitch(
                    title = S["settings.mask"],
                    description = S["settings.mask_desc"],
                    checked = settings.maskSecrets,
                    onCheckedChange = { value -> onUpdate { it.copy(maskSecrets = value) } },
                    icon = Icons.Rounded.Key
                )
            }
        }

        item(key = "storage") {
            SettingsSection(
                title = S["settings.storage"],
                subtitle = S["settings.storage_sub"],
                accent = scheme.secondary
            ) {
                OutlinedTextField(
                    value = settings.folderName,
                    onValueChange = { value ->
                        onUpdate { it.copy(folderName = AppSettings.sanitizeFolder(value)) }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    label = { Text(S["settings.folder"]) },
                    leadingIcon = { Icon(Icons.Rounded.Folder, contentDescription = null) },
                    supportingText = {
                        Text(S.f("settings.downloads_path", settings.safeFolderName))
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = S["settings.retention"],
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = if (retentionDraft.toInt() == 0) {
                                S["settings.unlimited"]
                            } else {
                                S.f("settings.days", retentionDraft.toInt())
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = scheme.primary
                        )
                    }
                    Slider(
                        value = retentionDraft,
                        onValueChange = { value -> retentionDraft = value },
                        onValueChangeFinished = {
                            val days = retentionDraft.toInt()
                            if (days != settings.retentionDays) {
                                onUpdate { it.copy(retentionDays = days) }
                            }
                        },
                        valueRange = 0f..365f
                    )
                }
                OutlinedTextField(
                    value = maxEntriesDraft,
                    onValueChange = { value ->
                        maxEntriesDraft = value.filter { it.isDigit() }.take(6)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .onFocusChanged { state ->
                            if (!state.isFocused) commitMaxEntries()
                        },
                    label = { Text(S["settings.max_entries"]) },
                    supportingText = { Text(S["settings.max_entries_hint"]) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            commitMaxEntries()
                            focusManager.clearFocus()
                        }
                    ),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
                SettingsGroupDivider()
                SettingAction(
                    title = S["settings.export_all"],
                    description = S["settings.export_all_desc"],
                    icon = Icons.Rounded.Archive,
                    onClick = onExportAll
                )
                SettingsGroupDivider()
                SettingAction(
                    title = S["settings.import_backup"],
                    description = S["settings.import_backup_desc"],
                    icon = Icons.Rounded.RestorePage,
                    onClick = onImportBackup
                )
                SettingsGroupDivider()
                SettingAction(
                    title = S["settings.clear_journal"],
                    description = S["settings.clear_journal_desc"],
                    icon = Icons.Rounded.DeleteForever,
                    onClick = { showDelete = true },
                    tint = scheme.error
                )
            }
        }

        item(key = "appearance") {
            SettingsSection(
                title = S["settings.appearance"],
                subtitle = S["settings.appearance_sub"],
                accent = scheme.primary
            ) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = S["settings.theme"],
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        listOf(
                            "system" to S["settings.theme_system"],
                            "light" to S["settings.theme_light"],
                            "dark" to S["settings.theme_dark"]
                        )
                            .forEach { (value, label) ->
                                FilterChip(
                                    selected = settings.themeMode == value,
                                    onClick = { onUpdate { it.copy(themeMode = value) } },
                                    label = { Text(label) }
                                )
                            }
                    }
                }
                SettingsGroupDivider()
                SettingSwitch(
                    title = S["settings.dynamic"],
                    description = S["settings.dynamic_desc"],
                    checked = settings.dynamicColor,
                    onCheckedChange = { value -> onUpdate { it.copy(dynamicColor = value) } },
                    icon = Icons.Rounded.ColorLens
                )
                SettingsGroupDivider()
                SettingSwitch(
                    title = S["settings.compact"],
                    description = S["settings.compact_desc"],
                    checked = settings.compact,
                    onCheckedChange = { value -> onUpdate { it.copy(compact = value) } },
                    icon = Icons.Rounded.Tune
                )
                SettingsGroupDivider()
                SettingSwitch(
                    title = S["settings.show_raw"],
                    description = S["settings.show_raw_desc"],
                    checked = settings.showRawPayload,
                    onCheckedChange = { value -> onUpdate { it.copy(showRawPayload = value) } },
                    icon = Icons.Rounded.Code
                )
                SettingsGroupDivider()
                SettingAction(
                    title = S["settings.test"],
                    description = S["settings.test_desc"],
                    icon = Icons.Rounded.NotificationsActive,
                    onClick = onTest
                )
            }
        }

        item(key = "language") {
            SettingsSection(
                title = S["settings.language"],
                subtitle = S["settings.language_sub"],
                accent = scheme.primary
            ) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        AppLanguage.entries.forEach { language ->
                            FilterChip(
                                selected = currentLanguage == language,
                                onClick = {
                                    onUpdate { it.copy(language = language.code) }
                                    S.setLanguage(language)
                                    CaptureEngine.clearLabelCache()
                                    ShizukuManager.refresh()
                                    context.findActivity()?.recreate()
                                },
                                label = { Text(S[language.labelKey]) }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = S["settings.language_note"],
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
            }
        }

        item(key = "display") {
            SettingsSection(
                title = S["settings.display"],
                subtitle = S["settings.display_sub"],
                accent = scheme.secondary
            ) {
                SettingSwitch(
                    title = S["settings.adaptive"],
                    description = S["settings.adaptive_desc"],
                    checked = settings.adaptiveLayout,
                    onCheckedChange = { value -> onUpdate { it.copy(adaptiveLayout = value) } },
                    icon = Icons.Rounded.Tune
                )
                SettingsGroupDivider()
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = S["settings.scale"],
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = S.f("settings.scale_value", (scaleDraft * 100).roundToInt()),
                            style = MaterialTheme.typography.labelMedium,
                            color = scheme.primary
                        )
                    }
                    Slider(
                        value = scaleDraft,
                        onValueChange = { value -> scaleDraft = value },
                        onValueChangeFinished = {
                            if (scaleDraft != settings.uiScale) {
                                onUpdate { it.copy(uiScale = scaleDraft) }
                            }
                        },
                        valueRange = 0.85f..1.15f,
                        enabled = settings.adaptiveLayout
                    )
                    Text(
                        text = S.f(
                            "settings.device_info",
                            device.widthDp,
                            device.heightDp,
                            device.scale,
                            device.density
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                    Text(
                        text = S.f(
                            "settings.device_class",
                            S[
                                when (device.screenClass) {
                                    ScreenClass.COMPACT -> "settings.class_compact"
                                    ScreenClass.MEDIUM -> "settings.class_medium"
                                    ScreenClass.EXPANDED -> "settings.class_expanded"
                                }
                            ]
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = S["settings.adaptive_note"],
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
            }
        }

        item(key = "about") {
            SettingsSection(
                title = S["settings.about"],
                subtitle = S["settings.about_sub"],
                accent = scheme.outline
            ) {
                SettingValueRow(
                    title = S["settings.version"],
                    value = BuildConfig.VERSION_NAME,
                    icon = Icons.Rounded.AutoAwesome
                )
                SettingsGroupDivider()
                SettingAction(
                    title = S["settings.policy"],
                    description = S["settings.policy_desc"],
                    icon = Icons.Rounded.Security,
                    onClick = { onOpenUrl("https://github.com/HmnDev-Tech/chronics") }
                )
                SettingsGroupDivider()
                Text(
                    text = S.f("settings.privacy_note", settings.safeFolderName),
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text(S["settings.clear_title"]) },
            text = {
                Text(S["settings.clear_body"])
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDelete = false
                        onDeleteAll()
                    }
                ) { Text(S["settings.clear_confirm"], color = scheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) { Text(S["common.cancel"]) }
            }
        )
    }
}

@Composable
private fun AccessRow(
    title: String,
    description: String,
    granted: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingIcon(
            icon = if (granted) Icons.Rounded.Security else Icons.Rounded.Key,
            tint = if (granted) Lime else Coral
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        TextButton(onClick = onClick) {
            Text(if (granted) S["common.have"] else S["common.grant"])
        }
    }
}

private fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
