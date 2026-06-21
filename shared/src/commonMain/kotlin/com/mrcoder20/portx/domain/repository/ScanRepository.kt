package com.mrcoder20.portx.domain.repository

import com.mrcoder20.portx.data.network.ScanConfig
import com.mrcoder20.portx.domain.model.ScanResult
import kotlinx.coroutines.flow.Flow

interface ScanRepository {
    suspend fun scanPorts(
        config: ScanConfig,
        onProgress: (Int) -> Unit = {}
    ): ScanResult
    fun getAllScans(): Flow<List<ScanResult>>
    fun getLatestScan(): Flow<ScanResult?>
    suspend fun saveScan(scan: ScanResult)
    suspend fun deleteScan(id: Long)
    suspend fun deleteAllScans()
}
