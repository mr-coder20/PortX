package com.mrcoder20.portx.domain

import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.Socket
import java.util.Scanner

class JvmNetworkTools : NetworkTools {
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
                    emit(PingResult(i + 1, time, true, "Reply from $ipStr: bytes=32 time=${time}ms TTL=128"))
                } else {
                    emit(PingResult(i + 1, null, false, "Request timed out for $ipStr"))
                }
                kotlinx.coroutines.delay(800)
            }
        } catch (e: Exception) {
            emit(PingResult(0, null, false, "Ping Error: ${e.localizedMessage}"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun dnsLookup(host: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val results = InetAddress.getAllByName(host.trim()).map { it.hostAddress ?: "" }
            results
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun whois(host: String): String = withContext(Dispatchers.IO) {
        try {
            val cleanHost = host.trim().lowercase().removePrefix("https://").removePrefix("http://").removePrefix("www.")
            val socket = Socket("whois.iana.org", 43)
            socket.soTimeout = 7000
            val out = socket.getOutputStream()
            out.write((cleanHost + "\r\n").toByteArray())
            out.flush()
            
            val scanner = Scanner(socket.getInputStream())
            val sb = StringBuilder()
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine()).append("\n")
            }
            socket.close()
            
            val result = sb.toString()
            if (result.contains("whois:", true)) {
                val nextServer = result.lines()
                    .find { it.contains("whois:", true) && !it.contains("iana.org") }
                    ?.substringAfter(":")?.trim() ?: return@withContext result
                
                if (nextServer.isBlank()) return@withContext result

                try {
                    val socket2 = Socket(nextServer, 43)
                    socket2.soTimeout = 7000
                    socket2.getOutputStream().write((cleanHost + "\r\n").toByteArray())
                    val scanner2 = Scanner(socket2.getInputStream())
                    val sb2 = StringBuilder()
                    while (scanner2.hasNextLine()) {
                        sb2.append(scanner2.nextLine()).append("\n")
                    }
                    socket2.close()
                    sb2.toString()
                } catch (e: Exception) {
                    result + "\n\n[Authority Redirect to $nextServer failed: ${e.message}]"
                }
            } else {
                result
            }
        } catch (e: Exception) {
            "WHOIS Resolution Error: ${e.localizedMessage}. Ensure you are entering a valid domain (e.g. google.com)."
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
                val response = client.get(url)
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
        var ip = "127.0.0.1"
        var name = "unknown"
        var isWifi = false
        
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            if (interfaces != null) {
                while (interfaces.hasMoreElements()) {
                    val iface = interfaces.nextElement()
                    if (iface.isLoopback || !iface.isUp) continue
                    
                    val dispName = iface.displayName.lowercase()
                    if (dispName.contains("wi-fi") || dispName.contains("wlan") || iface.name.lowercase().contains("wlan")) {
                        isWifi = true
                    }

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
        } catch (e: Exception) {}

        return LocalIpInfo(ip, name, isWifi)
    }
}

actual fun getNetworkTools(): NetworkTools = JvmNetworkTools()
