package com.mrcoder20.portx.domain

import com.mrcoder20.portx.data.network.ScanConfig
import com.mrcoder20.portx.domain.usecase.ScanPortUseCase
import kotlinx.coroutines.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class JvmScannerController : ScannerController, KoinComponent {
    private val scanPortUseCase: ScanPortUseCase by inject()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var scanJob: Job? = null

    override fun startScan(config: ScanConfig) {
        scanJob?.cancel()
        scanJob = scope.launch {
            ScanManager.setScanning(true)
            try {
                val result = scanPortUseCase(config) { progress ->
                    ScanManager.updateProgress(progress)
                }
                
                ScanManager.setResult(result)
            } catch (e: Exception) {
                ScanManager.setError(e.message ?: "Scan failed")
            } finally {
                ScanManager.setScanning(false)
            }
        }
    }

    override fun stopScan() {
        scanJob?.cancel()
        ScanManager.setScanning(false)
    }
}

actual fun getScannerController(): ScannerController = JvmScannerController()
