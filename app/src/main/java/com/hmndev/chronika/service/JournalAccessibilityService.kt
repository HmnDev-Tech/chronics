package com.hmndev.chronika.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.hmndev.chronika.data.SettingsStore

class JournalAccessibilityService : AccessibilityService() {
    override fun onServiceConnected() {
        super.onServiceConnected()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val settings = SettingsStore.current()
        if (!settings.captureEnabled || !settings.captureAccessibility) return
        val packageName = event.packageName?.toString().orEmpty()
        if (packageName.isBlank()) return
        if (packageName == this.packageName) return

        when (event.eventType) {
            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
                val texts = event.text.mapNotNull { it?.toString() }
                CaptureEngine.captureAccessibility(
                    this,
                    packageName,
                    texts,
                    event.contentDescription?.toString()
                )
            }

            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                if (!settings.deepAccessibility) return
                if (packageName != "com.android.systemui") return
                val className = event.className?.toString().orEmpty()
                if (!className.contains("Notification", ignoreCase = true) &&
                    !className.contains("Shade", ignoreCase = true)
                ) return
                val texts = event.text.mapNotNull { it?.toString() }
                    .filter { it.trim().length > 2 }
                if (texts.isEmpty()) return
                CaptureEngine.captureAccessibility(
                    this,
                    packageName,
                    texts,
                    event.contentDescription?.toString()
                )
            }
        }
    }

    override fun onInterrupt() = Unit
}
