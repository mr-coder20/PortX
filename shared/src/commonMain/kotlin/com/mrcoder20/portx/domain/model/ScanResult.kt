package com.mrcoder20.portx.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ScanResult(
    val id: Long? = null,
    val target: String,
    val openPorts: List<Int>,
    val portBanners: Map<Int, String> = emptyMap(),
    val portServices: Map<Int, String> = emptyMap(),
    val timestamp: Long,
    val securityScore: Int,
    val deviceName: String? = null,
    val osFingerprint: String? = null,
    val scanType: String? = "TCP",
    val bannerGrabbing: Boolean = false,
    val concurrentScans: Int = 100,
    val timeout: Int = 1000
)
