package com.mrcoder20.portx.domain

import android.content.ClipData
import android.content.ClipboardManager as AndroidClipboard
import android.content.Context
import android.widget.Toast
import com.mrcoder20.portx.appContext

class AndroidClipboardManager : ClipboardManager {
    override fun copyToClipboard(text: String) {
        val clipboard = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as AndroidClipboard
        val clip = ClipData.newPlainText("PortX Data", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(appContext, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }
}

actual fun getClipboardManager(): ClipboardManager = AndroidClipboardManager()
