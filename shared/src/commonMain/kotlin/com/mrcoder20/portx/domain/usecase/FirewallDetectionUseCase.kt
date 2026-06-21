package com.mrcoder20.portx.domain.usecase

import com.mrcoder20.portx.domain.model.ScanResult

class FirewallDetectionUseCase {
    operator fun invoke(scanResult: ScanResult): String {
        val openCount = scanResult.openPorts.size
        
        return when {
            openCount == 0 -> "High Probability of Firewall (All ports dropped)"
            openCount < 2 -> "Possible Firewall Detected (Very few ports open)"
            else -> "No Firewall Detected"
        }
    }
}
