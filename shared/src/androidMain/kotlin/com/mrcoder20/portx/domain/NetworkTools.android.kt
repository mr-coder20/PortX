package com.mrcoder20.portx.domain

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.mrcoder20.portx.appContext
import io.ktor.client.statement.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.NetworkInterface

class AndroidNetworkTools : NetworkTools {
    override suspend fun ping(host: String): Flow<PingResult> = flow {
        try {
            val address = try {
                InetAddress.getByName(host.trim())
            } catch (e: Exception) {
                null
            }

            if (address == null) {
                emit(PingResult(0, null, false, "Error: Unknown host $host"))
                return@flow
            }

            val ipStr = address.hostAddress
            emit(PingResult(0, null, true, "Pinging $host [$ipStr] with 32 bytes of data:"))

            repeat(4) { i ->
                val start = System.currentTimeMillis()
                val reachable = address.isReachable(2000)
                val end = System.currentTimeMillis()
                val time = end - start
                
                if (reachable) {
                    emit(PingResult(i + 1, time, true, "Reply from $ipStr: bytes=32 time=${time}ms"))
                } else {
                    emit(PingResult(i + 1, null, false, "Request timed out for $ipStr"))
                }
                kotlinx.coroutines.delay(800)
            }
        } catch (e: Exception) {
            emit(PingResult(0, null, false, "Ping Error: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun dnsLookup(host: String): List<String> {
        return try {
            InetAddress.getAllByName(host.trim()).map { it.hostAddress ?: "" }.filter { it.isNotEmpty() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun whois(host: String): String = withContext(Dispatchers.IO) {
        try {
            val cleanHost = host.trim().lowercase().removePrefix("https://").removePrefix("http://").removePrefix("www.")
            val client = SecurityHarden.createSecureClient()
            val response: HttpResponse = client.get("https://rdap.org/domain/$cleanHost")
            if (response.status.value in 200..299) {
                response.bodyAsText().take(5000)
            } else {
                "WHOIS data not available for $cleanHost via RDAP."
            }
        } catch (e: Exception) {
            "WHOIS Resolution Error (Mobile): ${e.message}. Domain might be invalid or RDAP is blocked."
        }
    }

    override suspend fun getPublicIp(): String? = withContext(Dispatchers.IO) {
        val providers = listOf(
            "https://api.ipify.org",
            "https://ifconfig.me/ip",
            "https://icanhazip.com",
            "https://ident.me"
        )
        
        val client = SecurityHarden.createSecureClient()
        providers.forEach { url ->
            try {
                val response: HttpResponse = client.get(url)
                if (response.status.value in 200..299) {
                    val ip = response.bodyAsText().trim()
                    if (ip.isNotEmpty()) return@withContext ip
                }
            } catch (e: Exception) {
                // Try next
            }
        }
        null
    }

    override fun getLocalIpInfo(): LocalIpInfo {
        return try {
            val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val activeNetwork = cm?.activeNetwork
            val caps = cm?.getNetworkCapabilities(activeNetwork)
            val isWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true

            var ip = "0.0.0.0"
            var name = "unknown"
            
            val interfaces = NetworkInterface.getNetworkInterfaces()
            if (interfaces != null) {
                while (interfaces.hasMoreElements()) {
                    val iface = interfaces.nextElement()
                    if (iface.isLoopback || !iface.isUp) continue
                    val addresses = iface.inetAddresses
                    while (addresses.hasMoreElements()) {
                        val addr = addresses.nextElement()
                        val hostAddr = addr.hostAddress
                        if (hostAddr == null || hostAddr.contains(":")) continue 
                        ip = hostAddr
                        name = iface.displayName
                    }
                }
            }
            LocalIpInfo(ip, name, isWifi)
        } catch (e: Exception) {
            LocalIpInfo("0.0.0.0", "Error", false)
        }
    }
}

actual fun getNetworkTools(): NetworkTools = AndroidNetworkTools()
