package com.hmndev.chronika

import android.app.Application
import com.hmndev.chronika.data.JournalDb
import com.hmndev.chronika.data.SettingsStore
import com.hmndev.chronika.i18n.AppLanguage
import com.hmndev.chronika.i18n.S
import com.hmndev.chronika.shizuku.ShizukuManager

class ChronikaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SettingsStore.init(this)
        S.setLanguage(AppLanguage.resolve(SettingsStore.current().language))
        JournalDb.get(this)
        ShizukuManager.init()
        ShizukuManager.refresh()
    }
}
