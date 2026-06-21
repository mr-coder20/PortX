package com.mrcoder20.portx.domain

import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.mrcoder20.portx.appContext
import com.mrcoder20.portx.data.network.ScanConfig
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AndroidScannerController : ScannerController {
    @OptIn(DelicateCoroutinesApi::class)
    override fun startScan(config: ScanConfig) {
        // Request notification permission if needed on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {
                GlobalScope.launch {
                    ScanManager.triggerUiEvent(ScanUIEvent.RequestNotificationPermission)
                }
            }
        }

        val intent = Intent().apply {
            setClassName(appContext.packageName, "com.mrcoder20.portx.android.ScannerService")
            action = "START_SCAN"
            putExtra("config", Json.encodeToString(config))
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
             appContext.startForegroundService(intent)
        } else {
             appContext.startService(intent)
        }
    }

    override fun stopScan() {
        val intent = Intent().apply {
            setClassName(appContext.packageName, "com.mrcoder20.portx.android.ScannerService")
            action = "STOP_SCAN"
        }
        appContext.startService(intent)
    }
}

actual fun getScannerController(): ScannerController = AndroidScannerController()
