package com.hmndev.chronika

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hmndev.chronika.ui.ChronikaRoot
import com.hmndev.chronika.ui.JournalViewModel
import com.hmndev.chronika.ui.theme.ChronikaTheme

class MainActivity : ComponentActivity() {
    private val viewModel: JournalViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestRuntimePermissionsIfNeeded()
        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            ChronikaTheme(settings = settings) {
                ChronikaRoot(viewModel = viewModel)
            }
        }
    }

    private fun requestRuntimePermissionsIfNeeded() {
        val wanted = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
        val missing = wanted.filter {
            checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            requestPermissions(missing.toTypedArray(), 2201)
        }
    }
}
