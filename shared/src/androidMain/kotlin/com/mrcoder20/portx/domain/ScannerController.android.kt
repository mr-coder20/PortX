package com.mrcoder20.portx.domain

import android.content.Intent
import com.mrcoder20.portx.appContext
import com.mrcoder20.portx.data.network.ScanConfig
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AndroidScannerController : ScannerController {
    override fun startScan(config: ScanConfig) {
        val intent = Intent().apply {
            setClassName(appContext.packageName, "com.mrcoder20.portx.android.ScannerService")
            action = "START_SCAN"
            putExtra("config", Json.encodeToString(config))
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION.SDK_INT) { // simplified for now
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
