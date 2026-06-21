package com.mrcoder20.portx.domain

import java.io.File
import javax.swing.JFileChooser
import javax.swing.SwingUtilities
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.filechooser.FileSystemView

class JvmFileSharer : FileSharer {
    override fun shareFile(content: String, fileName: String, mimeType: String) {
        SwingUtilities.invokeLater {
            try {
                // Initialize JFileChooser with standard home directory to avoid NPE with system nodes
                val fsv = FileSystemView.getFileSystemView()
                val fileChooser = JFileChooser(fsv.homeDirectory).apply {
                    dialogTitle = "Save Scan Report"
                    selectedFile = File(fileName)
                    fileFilter = FileNameExtensionFilter("Scan Report", fileName.substringAfterLast("."))
                }
                
                val result = fileChooser.showSaveDialog(null)
                if (result == JFileChooser.APPROVE_OPTION) {
                    val file = fileChooser.selectedFile
                    file.writeText(content)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun downloadFile(content: String, fileName: String, mimeType: String): String? {
        return try {
            val userHome = System.getProperty("user.home")
            val downloadsDir = File(userHome, "Downloads")
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            
            val file = File(downloadsDir, fileName)
            file.writeText(content)
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}

actual fun getFileSharer(): FileSharer = JvmFileSharer()
