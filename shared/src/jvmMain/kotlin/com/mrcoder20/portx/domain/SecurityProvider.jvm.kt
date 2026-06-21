package com.mrcoder20.portx.domain

class JvmSecurityProvider : SecurityProvider {
    override fun isDeviceCompromised(): Boolean = false
    override fun getSecurityMessage(): String? = null
}

actual fun getSecurityProvider(): SecurityProvider = JvmSecurityProvider()
