package com.hmndev.chronika.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hmndev.chronika.ui.components.AppIcon
import com.hmndev.chronika.ui.theme.Coral
import com.hmndev.chronika.ui.theme.Lime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class InstalledApp(
    val packageName: String,
    val label: String
)

@Composable
fun AppPickerScreen(
    selected: Set<String>,
    includedOnly: Boolean,
    onToggle: (String) -> Unit,
    onSetIncludedOnly: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    var query by rememberSaveable { mutableStateOf("") }
    val apps by produceState<List<InstalledApp>>(emptyList()) {
        value = withContext(Dispatchers.IO) { loadLaunchableApps(context) }
    }
    val filtered = remember(apps, query) {
        if (query.isBlank()) {
            apps
        } else {
            apps.filter {
                it.label.contains(query, ignoreCase = true) ||
                    it.packageName.contains(query, ignoreCase = true)
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = scheme.background
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Назад")
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "ПРИЛОЖЕНИЯ",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.primary
                    )
                    Text(
                        text = if (includedOnly) "Белый список" else "Исключения",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = selected.size.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.primary
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModeButton(
                    title = "Исключать",
                    selected = !includedOnly,
                    accent = Coral,
                    onClick = { onSetIncludedOnly(false) },
                    modifier = Modifier.weight(1f)
                )
                ModeButton(
                    title = "Только выбранные",
                    selected = includedOnly,
                    accent = Lime,
                    onClick = { onSetIncludedOnly(true) },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                singleLine = true,
                shape = CircleShape,
                placeholder = { Text("Поиск приложения…") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotBlank()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Очистить")
                        }
                    }
                }
            )
            Text(
                text = if (includedOnly) {
                    "Сохраняются уведомления только отмеченных приложений."
                } else {
                    "Отмеченные приложения не попадают в журнал."
                },
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filtered, key = { it.packageName }) { app ->
                    val checked = selected.contains(app.packageName)
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = if (checked) {
                            scheme.primaryContainer.copy(alpha = 0.6f)
                        } else {
                            scheme.surfaceContainerLow
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggle(app.packageName) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppIcon(
                                packageName = app.packageName,
                                label = app.label,
                                size = 38.dp
                            )
                            Spacer(Modifier.width(11.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = app.label,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = app.packageName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = scheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { onToggle(app.packageName) }
                            )
                        }
                    }
                }
                if (filtered.isEmpty()) {
                    item(key = "empty") {
                        Text(
                            text = "Ничего не найдено",
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            Button(
                onClick = onBack,
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Text("Готово")
            }
        }
    }
}

@Composable
private fun ModeButton(
    title: String,
    selected: Boolean,
    accent: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) accent.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceContainerLow,
        border = if (selected) {
            androidx.compose.foundation.BorderStroke(
                1.dp,
                accent.copy(alpha = 0.7f)
            )
        } else {
            null
        }
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        )
    }
}

private fun loadLaunchableApps(context: Context): List<InstalledApp> {
    val manager = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    @Suppress("DEPRECATION")
    val resolved = manager.queryIntentActivities(intent, 0)
    return resolved
        .mapNotNull { info ->
            val activity = info.activityInfo ?: return@mapNotNull null
            val packageName = activity.packageName ?: return@mapNotNull null
            if (packageName == context.packageName) return@mapNotNull null
            val label = runCatching {
                manager.getApplicationLabel(info.activityInfo.applicationInfo).toString()
            }.getOrDefault(packageName)
            InstalledApp(packageName, label)
        }
        .distinctBy { it.packageName }
        .sortedBy { it.label.lowercase() }
}
