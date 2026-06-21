package com.mrcoder20.portx.domain.usecase

import com.mrcoder20.portx.domain.model.ScanResult

class SecurityScoreUseCase {
    operator fun invoke(scanResult: ScanResult): Int {
        var score = 100
        
        // Deduction for open ports
        score -= scanResult.openPorts.size * 5
        
        // Critical ports check
        val criticalPorts = listOf(21, 22, 23, 445, 3389)
        val openCriticalPorts = scanResult.openPorts.intersect(criticalPorts)
        score -= openCriticalPorts.size * 10
        
        // Ensure score is between 0 and 100
        return score.coerceIn(0, 100)
    }
}
