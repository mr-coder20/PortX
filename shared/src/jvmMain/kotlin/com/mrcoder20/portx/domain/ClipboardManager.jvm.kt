package com.mrcoder20.portx.domain

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

class JvmClipboardManager : ClipboardManager {
    override fun copyToClipboard(text: String) {
        val selection = StringSelection(text)
        Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, selection)
    }
}

actual fun getClipboardManager(): ClipboardManager = JvmClipboardManager()
