package com.mrcoder20.portx.android

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.mrcoder20.portx.data.network.ScanConfig
import com.mrcoder20.portx.domain.ScanManager
import com.mrcoder20.portx.domain.model.ScanResult
import com.mrcoder20.portx.domain.repository.ScanRepository
import com.mrcoder20.portx.domain.usecase.ScanPortUseCase
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import org.koin.android.ext.android.inject

class ScannerService : Service() {

    private val scanPortUseCase: ScanPortUseCase by inject()
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var scanJob: Job? = null

    private val CHANNEL_ID = "scanner_channel"
    private val NOTIFICATION_ID = 1
    
    private var lastNotificationUpdateTime = 0L
    private val MIN_NOTIFICATION_INTERVAL = 500L // ms

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Critical: call startForeground immediately to avoid "Service.startForeground() not called" exception
        startForeground(NOTIFICATION_ID, createInitialNotification())
        
        when (intent?.action) {
            "START_SCAN" -> {
                val configJson = intent.getStringExtra("config")
                if (configJson != null) {
                    try {
                        val config = Json.decodeFromString<ScanConfig>(configJson)
                        startScan(config)
                    } catch (e: Exception) {
                        Log.e("ScannerService", "Failed to decode ScanConfig", e)
                        ScanManager.setError("Invalid scan configuration")
                        stopSelf()
                    }
                } else {
                    stopSelf()
                }
            }
            "STOP_SCAN" -> {
                stopScan()
            }
            else -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startScan(config: ScanConfig) {
        scanJob?.cancel()
        scanJob = serviceScope.launch {
            ScanManager.setScanning(true)
            
            try {
                val result = scanPortUseCase(config) { progress ->
                    ScanManager.updateProgress(progress)
                    maybeUpdateNotification(progress)
                }
                
                ScanManager.setResult(result)
                showFinishNotification(result)
            } catch (e: Exception) {
                Log.e("ScannerService", "Scan error", e)
                ScanManager.setError(e.message ?: "Scan failed")
            } finally {
                ScanManager.setScanning(false)
                withContext(Dispatchers.Main) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        }
    }

    private fun stopScan() {
        scanJob?.cancel()
        ScanManager.setScanning(false)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "PortX Scanner Engine",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active port scanning progress"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createInitialNotification(): Notification {
        return createNotificationBuilder(0)
            .setContentTitle("Initializing Engine...")
            .build()
    }

    private fun createNotificationBuilder(progress: Int): NotificationCompat.Builder {
        val stopIntent = Intent(this, ScannerService::class.java).apply {
            action = "STOP_SCAN"
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, 
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Scanning in Progress")
            .setContentText("Completed: $progress%")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop Scan", stopPendingIntent)
    }

    private fun maybeUpdateNotification(progress: Int) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastNotificationUpdateTime >= MIN_NOTIFICATION_INTERVAL || progress == 100) {
            val notification = createNotificationBuilder(progress).build()
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID, notification)
            lastNotificationUpdateTime = currentTime
        }
    }

    private fun showFinishNotification(result: ScanResult) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Scan Complete")
            .setContentText("Found ${result.openPorts.size} open ports on ${result.target}")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID + 1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
