package com.hmndev.chronika

import android.app.Application
import com.hmndev.chronika.data.JournalDb
import com.hmndev.chronika.data.SettingsStore
import com.hmndev.chronika.shizuku.ShizukuManager

class ChronikaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SettingsStore.init(this)
        JournalDb.get(this)
        ShizukuManager.init()
    }
}
