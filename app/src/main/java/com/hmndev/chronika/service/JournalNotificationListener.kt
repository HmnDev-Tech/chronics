package com.hmndev.chronika.service

import android.content.ComponentName
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class JournalNotificationListener : NotificationListenerService() {
    override fun onListenerConnected() {
        super.onListenerConnected()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        CaptureEngine.captureNotification(this, sbn)
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        runCatching {
            NotificationListenerService.requestRebind(
                ComponentName(this, JournalNotificationListener::class.java)
            )
        }
    }
}
