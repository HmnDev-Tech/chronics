package com.hmndev.chronika.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hmndev.chronika.BuildConfig
import com.hmndev.chronika.data.AppSettings
import com.hmndev.chronika.shizuku.ShizukuManager
import com.hmndev.chronika.ui.components.SettingAction
import com.hmndev.chronika.ui.components.SettingIcon
import com.hmndev.chronika.ui.components.SettingSwitch
import com.hmndev.chronika.ui.components.SettingValueRow
import com.hmndev.chronika.ui.components.SettingsGroupDivider
import com.hmndev.chronika.ui.components.SettingsSection
import com.hmndev.chronika.ui.theme.Coral
import com.hmndev.chronika.ui.theme.Lime
import com.hmndev.chronika.util.AccessState

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
    onDeleteAll: () -> Unit,
    onOpenUrl: (String) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val shizuku by ShizukuManager.state.collectAsState()
    var showDelete by remember { mutableStateOf(false) }
    var retentionDraft by remember(settings.retentionDays) {
        mutableStateOf(settings.retentionDays.toFloat())
    }
    var maxEntriesDraft by remember(settings.maxEntries) {
        mutableStateOf(settings.maxEntries.toString())
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
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 126.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item(key = "heading") {
            Column {
                Text(
                    text = "ПУЛЬТ",
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.primary
                )
                Text(
                    text = "Настройки",
                    style = MaterialTheme.typography.displaySmall,
                    color = scheme.onSurface
                )
                Text(
                    text = "Захват, фильтры, хранилище и доступы.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
            }
        }

        item(key = "access") {
            SettingsSection(
                title = "Доступы",
                subtitle = "Без них журнал не увидит уведомления",
                accent = Coral
            ) {
                AccessRow(
                    title = "Слушатель уведомлений",
                    description = "Основной источник полных текстов",
                    granted = access.listener,
                    onClick = onOpenNotificationSettings
                )
                SettingsGroupDivider()
                AccessRow(
                    title = "Служба доступности",
                    description = "Страховка для быстрых и удалённых уведомлений",
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
                            Text("Разрешить Shizuku")
                        }
                    }
                }
                if (shizuku.permissionGranted) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(onClick = onGrantListener, shape = CircleShape) {
                            Text("Выдать слушатель")
                        }
                        FilledTonalButton(onClick = onGrantAccessibility, shape = CircleShape) {
                            Text("Включить доступность")
                        }
                    }
                    SettingAction(
                        title = "Импортировать активные",
                        description = "Прочитать текущую панель через dumpsys notification",
                        icon = Icons.Rounded.CloudDownload,
                        onClick = onImportShizuku
                    )
                }
                SettingAction(
                    title = "Открыть Shizuku",
                    description = "Установить, запустить или проверить сервис",
                    icon = Icons.Rounded.Terminal,
                    onClick = onOpenShizuku
                )
            }
        }

        item(key = "capture") {
            SettingsSection(
                title = "Захват",
                subtitle = "Что и как сохранять",
                accent = Lime
            ) {
                SettingSwitch(
                    title = "Захват включён",
                    description = "Главный выключатель журнала",
                    checked = settings.captureEnabled,
                    onCheckedChange = { value -> onUpdate { it.copy(captureEnabled = value) } },
                    icon = Icons.Rounded.NotificationsActive
                )
                SettingsGroupDivider()
                SettingSwitch(
                    title = "Слушатель уведомлений",
                    description = "Полные заголовки, тексты, сообщения и действия",
                    checked = settings.captureListener,
                    onCheckedChange = { value -> onUpdate { it.copy(captureListener = value) } },
                    icon = Icons.Rounded.Bolt,
                    enabled = settings.captureEnabled
                )
                SettingsGroupDivider()
                SettingSwitch(
                    title = "Служба доступности",
                    description = "Текст событий уведомлений и панели",
                    checked = settings.captureAccessibility,
                    onCheckedChange = { value -> onUpdate { it.copy(captureAccessibility = value) } },
                    icon = Icons.Rounded.Accessibility,
                    enabled = settings.captureEnabled
                )
                SettingsGroupDivider()
                SettingSwitch(
                    title = "Глубокий захват панели",
                    description = "Читает текст открытой панели уведомлений (экспериментально)",
                    checked = settings.deepAccessibility,
                    onCheckedChange = { value -> onUpdate { it.copy(deepAccessibility = value) } },
                    icon = Icons.Rounded.Tune,
                    enabled = settings.captureEnabled && settings.captureAccessibility
                )
                SettingsGroupDivider()
                SettingSwitch(
                    title = "Автоэкспорт в Downloads",
                    description = "Каждая запись сразу получает JSON/TXT-файл",
                    checked = settings.autoExport,
                    onCheckedChange = { value -> onUpdate { it.copy(autoExport = value) } },
                    icon = Icons.Rounded.Download
                )
                SettingsGroupDivider()
                SettingSwitch(
                    title = "Сохранять изображения",
                    description = "Large icon и picture в папку media",
                    checked = settings.saveImages,
                    onCheckedChange = { value -> onUpdate { it.copy(saveImages = value) } },
                    icon = Icons.Rounded.ColorLens
                )
                SettingsGroupDivider()
                SettingSwitch(
                    title = "Сырые extras",
                    description = "Технический payload уведомления в JSON",
                    checked = settings.saveRawExtras,
                    onCheckedChange = { value -> onUpdate { it.copy(saveRawExtras = value) } },
                    icon = Icons.Rounded.Storage
                )
                SettingsGroupDivider()
                SettingSwitch(
                    title = "Склеивать повторы",
                    description = "Одинаковые обновления не создают новые записи",
                    checked = settings.dedupe,
                    onCheckedChange = { value -> onUpdate { it.copy(dedupe = value) } },
                    icon = Icons.Rounded.AutoAwesome
                )
                SettingsGroupDivider()
                SettingSwitch(
                    title = "Игнорировать постоянные",
                    description = "Плееры, загрузки, сервисные уведомления",
                    checked = settings.ignoreOngoing,
                    onCheckedChange = { value -> onUpdate { it.copy(ignoreOngoing = value) } },
                    icon = Icons.Rounded.CleaningServices
                )
                SettingsGroupDivider()
                SettingSwitch(
                    title = "Игнорировать тихие",
                    description = "Уведомления низкого приоритета",
                    checked = settings.ignoreSilent,
                    onCheckedChange = { value -> onUpdate { it.copy(ignoreSilent = value) } },
                    icon = Icons.Rounded.Bolt
                )
            }
        }

        item(key = "filters") {
            SettingsSection(
                title = "Фильтры",
                subtitle = "Какие приложения и слова пропускать",
                accent = scheme.tertiary
            ) {
                SettingAction(
                    title = if (settings.includedOnly) "Только выбранные приложения" else "Исключённые приложения",
                    description = if (settings.excludedApps.isEmpty()) {
                        "Список пуст"
                    } else {
                        "Выбрано: ${settings.excludedApps.size}"
                    },
                    icon = Icons.Rounded.Apps,
                    onClick = onOpenAppPicker,
                    trailing = settings.excludedApps.size.toString()
                )
                SettingsGroupDivider()
                SettingSwitch(
                    title = "Режим белого списка",
                    description = "Сохранять только выбранные приложения",
                    checked = settings.includedOnly,
                    onCheckedChange = { value -> onUpdate { it.copy(includedOnly = value) } },
                    icon = Icons.Rounded.Security
                )
                SettingsGroupDivider()
                OutlinedTextField(
                    value = settings.excludedKeywords,
                    onValueChange = { value -> onUpdate { it.copy(excludedKeywords = value) } },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    label = { Text("Стоп-слова через запятую") },
                    placeholder = { Text("реклама, акция, спам") },
                    shape = MaterialTheme.shapes.medium,
                    minLines = 1,
                    maxLines = 3
                )
                OutlinedTextField(
                    value = settings.requiredKeywords,
                    onValueChange = { value -> onUpdate { it.copy(requiredKeywords = value) } },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    label = { Text("Обязательные слова") },
                    placeholder = { Text("код, заказ, банк") },
                    shape = MaterialTheme.shapes.medium,
                    minLines = 1,
                    maxLines = 3
                )
                SettingsGroupDivider()
                SettingSwitch(
                    title = "Маскировать секреты",
                    description = "OTP, телефоны, e-mail и номера карт",
                    checked = settings.maskSecrets,
                    onCheckedChange = { value -> onUpdate { it.copy(maskSecrets = value) } },
                    icon = Icons.Rounded.Key
                )
            }
        }

        item(key = "storage") {
            SettingsSection(
                title = "Хранилище",
                subtitle = "Папка, срок жизни и размер архива",
                accent = scheme.secondary
            ) {
                OutlinedTextField(
                    value = settings.folderName,
                    onValueChange = { value ->
                        onUpdate { it.copy(folderName = value.replace("/", "_").take(48)) }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    label = { Text("Папка в Downloads") },
                    leadingIcon = { Icon(Icons.Rounded.Folder, contentDescription = null) },
                    supportingText = {
                        Text("Downloads/${settings.folderName.ifBlank { "Chronika" }}")
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Хранить записей",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = if (retentionDraft.toInt() == 0) {
                                "бессрочно"
                            } else {
                                "${retentionDraft.toInt()} дн."
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
                    label = { Text("Максимум записей в базе") },
                    supportingText = { Text("От 500 до 200000, старые записи удаляются первыми") },
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
                    title = "Экспортировать всё",
                    description = "Один JSON и один TXT со всеми записями",
                    icon = Icons.Rounded.Archive,
                    onClick = onExportAll
                )
                SettingsGroupDivider()
                SettingAction(
                    title = "Очистить журнал",
                    description = "Удалить записи из базы; файлы в Downloads останутся",
                    icon = Icons.Rounded.DeleteForever,
                    onClick = { showDelete = true },
                    tint = scheme.error
                )
            }
        }

        item(key = "appearance") {
            SettingsSection(
                title = "Оформление",
                subtitle = "Тема, цвет и плотность",
                accent = scheme.primary
            ) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = "Тема",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        listOf("system" to "Система", "light" to "Светлая", "dark" to "Тёмная")
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
                    title = "Динамический цвет",
                    description = "Палитра обоев на Android 12+",
                    checked = settings.dynamicColor,
                    onCheckedChange = { value -> onUpdate { it.copy(dynamicColor = value) } },
                    icon = Icons.Rounded.ColorLens
                )
                SettingsGroupDivider()
                SettingSwitch(
                    title = "Компактная плотность",
                    description = "Больше записей на экране",
                    checked = settings.compact,
                    onCheckedChange = { value -> onUpdate { it.copy(compact = value) } },
                    icon = Icons.Rounded.Tune
                )
                SettingsGroupDivider()
                SettingAction(
                    title = "Проверить уведомление",
                    description = "Отправить тестовое и найти его в журнале",
                    icon = Icons.Rounded.NotificationsActive,
                    onClick = onTest
                )
            }
        }

        item(key = "about") {
            SettingsSection(
                title = "О Хронике",
                subtitle = "Локальный архив без облака",
                accent = scheme.outline
            ) {
                SettingValueRow(
                    title = "Версия",
                    value = BuildConfig.VERSION_NAME,
                    icon = Icons.Rounded.AutoAwesome
                )
                SettingsGroupDivider()
                SettingAction(
                    title = "Политика и исходники",
                    description = "Приватный репозиторий проекта",
                    icon = Icons.Rounded.Security,
                    onClick = { onOpenUrl("https://github.com/HmnDev-Tech/chronika") }
                )
                SettingsGroupDivider()
                Text(
                    text = "Хроника не отправляет данные в сеть. Всё лежит в локальной базе " +
                        "и в Downloads/${settings.folderName.ifBlank { "Chronika" }}.",
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
            title = { Text("Очистить журнал?") },
            text = {
                Text(
                    "Будут удалены все записи из базы. Файлы в Downloads останутся " +
                        "нетронутыми — их можно удалить вручную."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDelete = false
                        onDeleteAll()
                    }
                ) { Text("Очистить", color = scheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) { Text("Отмена") }
            }
        )
    }
}

@Composable
private fun AccessRow(
    title: String,
    description: String,
    granted: Boolean,
    onClick: () -> Unit
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
            Text(if (granted) "ЕСТЬ" else "ВЫДАТЬ")
        }
    }
}
