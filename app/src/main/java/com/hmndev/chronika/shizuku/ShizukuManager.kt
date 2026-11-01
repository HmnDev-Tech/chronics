package com.hmndev.chronika.shizuku

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.hamondev.shevery.api.SheveryPrivilege
import com.hmndev.chronika.i18n.S
import com.hmndev.chronika.service.CaptureEngine
import com.hmndev.chronika.service.JournalAccessibilityService
import com.hmndev.chronika.service.JournalNotificationListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

enum class PrivilegedBackend {
    SHEVERY,
    SHIZUKU,
    NONE
}

data class ShizukuState(
    val available: Boolean = false,
    val permissionGranted: Boolean = false,
    val version: Int = 0,
    val message: String = S["shizuku.not_running"],
    val backend: PrivilegedBackend = PrivilegedBackend.NONE,
)

data class ShellResult(
    val ok: Boolean,
    val stdout: String = "",
    val stderr: String = "",
    val message: String = "",
)

data class ImportResult(
    val count: Int = 0,
    val message: String = "",
)

object ShizukuManager {
    const val REQUEST_CODE = 4102

    private val _state = MutableStateFlow(ShizukuState())
    val state: StateFlow<ShizukuState> = _state.asStateFlow()

    @Volatile
    private var initialized = false

    private val sheveryBinderReceived = SheveryPrivilege.OnBinderReceivedListener {
        refresh()
    }
    private val sheveryBinderDead = SheveryPrivilege.OnBinderDeadListener {
        refresh()
    }
    private val sheveryPermissionResult = SheveryPrivilege.OnPermissionResultListener { _, grantResult ->
        refresh(grantResult == PackageManager.PERMISSION_GRANTED)
    }

    private val binderReceived = Shizuku.OnBinderReceivedListener {
        refresh()
    }
    private val binderDead = Shizuku.OnBinderDeadListener {
        refresh()
    }
    private val permissionResult = Shizuku.OnRequestPermissionResultListener { _, grantResult ->
        refresh(grantResult == PackageManager.PERMISSION_GRANTED)
    }

    fun init(context: Context? = null) {
        if (!initialized) {
            initialized = true
            runCatching {
                SheveryPrivilege.addBinderReceivedListener(sheveryBinderReceived)
                SheveryPrivilege.addBinderDeadListener(sheveryBinderDead)
                SheveryPrivilege.addRequestPermissionResultListener(sheveryPermissionResult)
            }
            runCatching {
                Shizuku.addBinderReceivedListenerSticky(binderReceived)
                Shizuku.addBinderDeadListener(binderDead)
                Shizuku.addRequestPermissionResultListener(permissionResult)
            }
        }
        if (context != null) {
            runCatching { SheveryPrivilege.connectFast(context) }
        }
        refresh()
    }

    fun refresh(forcedPermission: Boolean? = null) {
        val sheveryAvailable = runCatching { SheveryPrivilege.pingBinder() }.getOrDefault(false)
        if (sheveryAvailable) {
            val version = runCatching { SheveryPrivilege.getVersion() }.getOrDefault(0)
            val permission = when {
                forcedPermission != null -> forcedPermission
                else -> runCatching {
                    SheveryPrivilege.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
                }.getOrDefault(false)
            }
            val message = when {
                !permission -> S["shizuku.permission_needed"]
                else -> S.f("shizuku.active", version).replace("Shizuku", "Shevery")
            }
            _state.value = ShizukuState(
                available = true,
                permissionGranted = permission,
                version = version,
                message = message,
                backend = PrivilegedBackend.SHEVERY
            )
            return
        }

        val shizukuAvailable = runCatching { Shizuku.pingBinder() }.getOrDefault(false)
        val version = if (shizukuAvailable) {
            runCatching { Shizuku.getVersion() }.getOrDefault(0)
        } else {
            0
        }
        val permission = when {
            !shizukuAvailable -> false
            forcedPermission != null -> forcedPermission
            runCatching { Shizuku.isPreV11() }.getOrDefault(true) -> false
            else -> runCatching {
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            }.getOrDefault(false)
        }
        val backend = if (shizukuAvailable) PrivilegedBackend.SHIZUKU else PrivilegedBackend.NONE
        val message = when {
            !shizukuAvailable -> S["shizuku.unavailable"]
            !permission -> S["shizuku.permission_needed"]
            else -> S.f("shizuku.active", version)
        }
        _state.value = ShizukuState(shizukuAvailable, permission, version, message, backend)
    }

    fun requestPermission() {
        val current = _state.value
        if (!current.available) {
            _state.value = current.copy(message = S["shizuku.start_first"])
            return
        }
        if (current.backend == PrivilegedBackend.SHEVERY || SheveryPrivilege.pingBinder()) {
            runCatching {
                SheveryPrivilege.requestPermission(REQUEST_CODE)
            }.onFailure {
                _state.value = current.copy(message = S.f("shizuku.request_failed", it.message))
            }
            return
        }
        runCatching {
            Shizuku.requestPermission(REQUEST_CODE)
        }.onFailure {
            _state.value = current.copy(message = S.f("shizuku.request_failed", it.message))
        }
    }

    suspend fun runCommand(command: String): ShellResult = withContext(Dispatchers.IO) {
        val current = _state.value
        if (!current.available) {
            return@withContext ShellResult(false, message = S["shizuku.not_running"])
        }
        if (!current.permissionGranted) {
            return@withContext ShellResult(false, message = S["shizuku.no_permission"])
        }
        var process: Process? = null
        try {
            val spawned: Process = if (current.backend == PrivilegedBackend.SHEVERY || SheveryPrivilege.pingBinder()) {
                SheveryPrivilege.newProcess(arrayOf("sh", "-c", command), null, null)
            } else {
                val method = Shizuku::class.java.getDeclaredMethod(
                    "newProcess",
                    emptyArray<String>()::class.java,
                    emptyArray<String>()::class.java,
                    String::class.java
                )
                method.isAccessible = true
                method.invoke(
                    null,
                    arrayOf("sh", "-c", command),
                    null,
                    null
                ) as Process
            }
            process = spawned
            val (stdout, stderr) = coroutineScope {
                val out = async(Dispatchers.IO) {
                    BufferedReader(InputStreamReader(spawned.inputStream)).use { it.readText() }
                }
                val err = async(Dispatchers.IO) {
                    BufferedReader(InputStreamReader(spawned.errorStream)).use { it.readText() }
                }
                out.await() to err.await()
            }
            val code = spawned.waitFor()
            ShellResult(
                ok = code == 0,
                stdout = stdout,
                stderr = stderr,
                message = if (code == 0) "" else S.f("shizuku.code", code, stderr.take(240))
            )
        } catch (t: Throwable) {
            ShellResult(false, message = t.message ?: t.javaClass.simpleName)
        } finally {
            runCatching { process?.destroy() }
        }
    }

    suspend fun grantNotificationListener(context: Context): ShellResult {
        val component = ComponentName(context, JournalNotificationListener::class.java)
            .flattenToString()
        val direct = runCommand("cmd notification allow_listener $component")
        if (direct.ok) {
            return direct.copy(message = S["shizuku.listener_granted"])
        }
        val existing = runCommand("settings get secure enabled_notification_listeners")
        if (!existing.ok) return direct
        val components = parseComponentList(existing.stdout)
        if (components.contains(component)) {
            return ShellResult(true, message = S["shizuku.already_granted"])
        }
        val next = (components + component).joinToString(":")
        val write = runCommand("settings put secure enabled_notification_listeners '$next'")
        return if (write.ok) {
            ShellResult(true, message = S["shizuku.listeners_updated"])
        } else {
            write
        }
    }

    suspend fun grantAccessibility(context: Context): ShellResult {
        val component = ComponentName(context, JournalAccessibilityService::class.java)
            .flattenToString()
        val existing = runCommand("settings get secure enabled_accessibility_services")
        val components = parseComponentList(existing.stdout)
        val next = if (components.contains(component)) {
            components.joinToString(":")
        } else {
            (components + component).joinToString(":")
        }
        val write = runCommand(
            "settings put secure enabled_accessibility_services '$next'; " +
                "settings put secure accessibility_enabled 1"
        )
        return if (write.ok) {
            ShellResult(true, message = S["shizuku.accessibility_enabled"])
        } else {
            write
        }
    }

    suspend fun importActiveNotifications(context: Context): ImportResult =
        withContext(Dispatchers.IO) {
            val dump = runCommand("dumpsys notification --noredact")
            if (!dump.ok) {
                return@withContext ImportResult(
                    message = dump.message.ifBlank { S["shizuku.read_failed"] }
                )
            }
            if (dump.stdout.isBlank()) {
                return@withContext ImportResult(message = S["shizuku.empty_dump"])
            }
            val count = CaptureEngine.importDump(context, dump.stdout)
            ImportResult(
                count = count,
                message = if (count > 0) {
                    S.f("shizuku.imported", count)
                } else {
                    S["shizuku.nothing_new"]
                }
            )
        }

    internal fun parseComponentList(raw: String): List<String> {
        val safeComponent = Regex("^[A-Za-z0-9._/$]+$")
        return raw.trim()
            .removePrefix("[")
            .removeSuffix("]")
            .split(':')
            .map { it.trim() }
            .filter { it.isNotEmpty() && it != "null" && safeComponent.matches(it) }
            .distinct()
    }
}
