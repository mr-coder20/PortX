package com.mrcoder20.portx.domain

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object SecurityHarden {
    private const val MASK = 0xAF

    fun transform(input: String): String {
        return input.map { (it.code xor MASK).toChar() }.joinToString("")
    }

    fun createSecureClient(): HttpClient {
        return HttpClient {
            install(ContentNegotiation) {
                json(Json { 
                    ignoreUnknownKeys = true 
                    prettyPrint = true
                })
            }
            
            install(HttpTimeout) {
                requestTimeoutMillis = 15000
                connectTimeoutMillis = 15000
                socketTimeoutMillis = 15000
            }

            defaultRequest {
                header("X-App-Integrity", "Secure-Handshake-V1")
                header("User-Agent", "PortX-Professional-Engine")
            }
        }
    }
}
