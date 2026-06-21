package com.mrcoder20.portx.domain

interface ClipboardManager {
    fun copyToClipboard(text: String)
}

expect fun getClipboardManager(): ClipboardManager
