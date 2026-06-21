package com.mrcoder20.portx.data.repository

import com.mrcoder20.portx.data.local.AppDatabase
import com.mrcoder20.portx.data.local.ScanEntity
import com.mrcoder20.portx.data.network.PortScanner
import com.mrcoder20.portx.domain.model.ScanResult
import com.mrcoder20.portx.domain.repository.ScanRepository
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Clock

class ScanRepositoryImpl(
    private val scanner: PortScanner,
    private val database: AppDatabase
) : ScanRepository {

    private val queries = database.appDatabaseQueries

    override suspend fun scanPorts(
        config: com.mrcoder20.portx.data.network.ScanConfig,
        onProgress: (Int) -> Unit
    ): ScanResult {
        val summary = scanner.scan(config, onProgress)
        val openResults = summary.results.filter { it.state == "open" }
        val scanResult = ScanResult(
            target = summary.target,
            openPorts = openResults.map { it.port }.distinct(),
            portBanners = openResults.associate { it.port to it.banner },
            portServices = openResults.associate { it.port to it.service },
            timestamp = Clock.System.now().toEpochMilliseconds(),
            securityScore = calculateScore(openResults.size),
            scanType = config.scanType,
            bannerGrabbing = config.serviceDetect,
            concurrentScans = config.concurrency,
            timeout = config.timeoutMs
        )
        return scanResult
    }

    override fun getAllScans(): Flow<List<ScanResult>> {
        return queries.selectAllScans()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }

    override fun getLatestScan(): Flow<ScanResult?> {
        return queries.selectLatestScan()
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomain() }
    }

    override suspend fun saveScan(scan: ScanResult) {
        withContext(Dispatchers.IO) {
            database.transaction {
                queries.insertScan(
                    target = scan.target,
                    openPorts = scan.openPorts,
                    portBanners = scan.portBanners,
                    portServices = scan.portServices,
                    timestamp = scan.timestamp,
                    securityScore = scan.securityScore.toLong(),
                    deviceName = scan.deviceName,
                    osFingerprint = scan.osFingerprint,
                    scanType = scan.scanType ?: "TCP",
                    bannerGrabbing = scan.bannerGrabbing,
                    concurrentScans = scan.concurrentScans.toLong(),
                    timeout = scan.timeout.toLong()
                )
            }
        }
    }

    override suspend fun deleteScan(id: Long) {
        withContext(Dispatchers.IO) {
            queries.deleteScanById(id)
        }
    }

    override suspend fun deleteAllScans() {
        withContext(Dispatchers.IO) {
            queries.deleteAllScans()
        }
    }

    private fun calculateScore(openPortsCount: Int): Int {
        return when {
            openPortsCount == 0 -> 100
            openPortsCount < 5 -> 80
            openPortsCount < 20 -> 50
            else -> 20
        }
    }

    private fun ScanEntity.toDomain(): ScanResult {
        return ScanResult(
            id = id,
            target = target,
            openPorts = openPorts,
            portBanners = portBanners,
            portServices = portServices,
            timestamp = timestamp,
            securityScore = securityScore.toInt(),
            deviceName = deviceName,
            osFingerprint = osFingerprint,
            scanType = scanType ?: "TCP",
            bannerGrabbing = bannerGrabbing,
            concurrentScans = concurrentScans.toInt(),
            timeout = timeout.toInt()
        )
    }
}
