package com.mrcoder20.portx.domain

interface SecurityProvider {
    fun isDeviceCompromised(): Boolean
    fun getSecurityMessage(): String?
}

expect fun getSecurityProvider(): SecurityProvider
