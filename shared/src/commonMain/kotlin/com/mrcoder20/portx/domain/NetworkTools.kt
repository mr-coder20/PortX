package com.mrcoder20.portx.domain

import kotlinx.coroutines.flow.Flow

interface NetworkTools {
    suspend fun ping(host: String): Flow<PingResult>
    suspend fun dnsLookup(host: String): List<String>
    suspend fun whois(host: String): String
    fun getLocalIpInfo(): LocalIpInfo
    suspend fun getPublicIp(): String?
}

data class PingResult(
    val sequence: Int,
    val timeMs: Long?,
    val isSuccess: Boolean,
    val message: String
)

data class LocalIpInfo(
    val ipAddress: String,
    val interfaceName: String,
    val isWifi: Boolean
)

expect fun getNetworkTools(): NetworkTools
