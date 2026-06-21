package com.mrcoder20.portx.domain

import java.io.File

class AndroidSecurityProvider : SecurityProvider {
    override fun isDeviceCompromised(): Boolean {
        return checkRootFiles() || checkSuBinary() || checkSystemProperties() || 
               isEmulator() || isDebuggerConnected() || isHookingDetected()
    }

    override fun getSecurityMessage(): String? {
        val message = when {
            checkRootFiles() || checkSuBinary() -> "Security Alert: Rooted Device Detected."
            isDebuggerConnected() -> "Security Alert: Active Debugger Detected."
            isEmulator() -> "Security Alert: Virtual Environment (Emulator) Detected."
            isHookingDetected() -> "Security Alert: Runtime Instrumentation (Hooking) Detected."
            else -> null
        }
        return message?.let { "$it Access restricted for system integrity." }
    }

    private fun isDebuggerConnected(): Boolean = android.os.Debug.isDebuggerConnected()

    private fun isEmulator(): Boolean {
        val model = android.os.Build.MODEL
        val hardware = android.os.Build.HARDWARE
        return model.contains("sdk", true) || model.contains("Emulator", true) || 
               hardware.contains("goldfish", true) || hardware.contains("ranchu", true)
    }

    private fun isHookingDetected(): Boolean {
        return try {
            val libraries = File("/proc/self/maps").readLines()
            libraries.any { line ->
                line.contains("frida", true) || line.contains("xposed", true) || 
                line.contains("substrate", true) || line.contains("magisk", true)
            }
        } catch (e: Exception) { false }
    }

    private fun checkRootFiles(): Boolean {
        val paths = arrayOf("/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su")
        return paths.any { File(it).exists() }
    }

    private fun checkSuBinary(): Boolean {
        return try {
            Runtime.getRuntime().exec("which su").inputStream.bufferedReader().readLine() != null
        } catch (e: Exception) { false }
    }

    private fun checkSystemProperties(): Boolean {
        val buildTags = android.os.Build.TAGS
        return buildTags != null && buildTags.contains("test-keys")
    }
}

actual fun getSecurityProvider(): SecurityProvider = AndroidSecurityProvider()
