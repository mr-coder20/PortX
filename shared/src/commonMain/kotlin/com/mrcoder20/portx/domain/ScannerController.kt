package com.mrcoder20.portx.domain

import com.mrcoder20.portx.data.network.ScanConfig

interface ScannerController {
    fun startScan(config: ScanConfig)
    fun stopScan()
}

expect fun getScannerController(): ScannerController
