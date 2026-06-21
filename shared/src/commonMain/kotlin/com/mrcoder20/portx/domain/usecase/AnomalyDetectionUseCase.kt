package com.mrcoder20.portx.domain.usecase

import com.mrcoder20.portx.domain.model.ScanResult

class AnomalyDetectionUseCase {
    operator fun invoke(scanResult: ScanResult): List<String> {
        val anomalies = mutableListOf<String>()
        
        // Critical/Suspicious ports
        val suspiciousPorts = listOf(23, 445, 135, 139, 3389, 5900)
        val openSuspicious = scanResult.openPorts.intersect(suspiciousPorts)
        if (openSuspicious.isNotEmpty()) {
            anomalies.add("High Risk: Dangerous ports open (${openSuspicious.joinToString(", ")})")
        }
        
        // Uncommon ports for consumer devices
        val uncommonPorts = scanResult.openPorts.filter { it > 1024 && it < 10000 }
        if (uncommonPorts.size > 5) {
            anomalies.add("Warning: High number of uncommon open ports")
        }
        
        return anomalies
    }
}
