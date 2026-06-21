package com.mrcoder20.portx.domain.usecase

import com.mrcoder20.portx.data.network.ScanConfig
import com.mrcoder20.portx.domain.model.ScanResult
import com.mrcoder20.portx.domain.repository.ScanRepository

class ScanPortUseCase(private val repository: ScanRepository) {
    suspend operator fun invoke(
        config: ScanConfig,
        onProgress: (Int) -> Unit = {}
    ): ScanResult {
        // Step 1: Perform the scan
        val result = repository.scanPorts(config, onProgress)
        
        // Step 2: Centralized Save (Guarantees persistence across all platforms)
        repository.saveScan(result)
        
        return result
    }
}
