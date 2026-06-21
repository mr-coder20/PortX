package com.mrcoder20.portx.domain

import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.mrcoder20.portx.appContext
import java.io.File
import java.io.FileOutputStream

class AndroidFileSharer : FileSharer {
    override fun shareFile(content: String, fileName: String, mimeType: String) {
        val cacheDir = File(appContext.cacheDir, "exports")
        if (!cacheDir.exists()) cacheDir.mkdirs()
        
        val file = File(cacheDir, fileName)
        file.writeText(content)
        
        val uri = FileProvider.getUriForFile(
            appContext,
            "${appContext.packageName}.fileprovider",
            file
        )
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        val chooserIntent = Intent.createChooser(intent, "Share Scan Report").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        appContext.startActivity(chooserIntent)
    }

    override fun downloadFile(content: String, fileName: String, mimeType: String): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                
                val resolver = appContext.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                
                uri?.let {
                    resolver.openOutputStream(it)?.use { outputStream ->
                        outputStream.write(content.toByteArray())
                    }
                    "Downloads/$fileName"
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, fileName)
                FileOutputStream(file).use { it.write(content.toByteArray()) }
                "Downloads/$fileName"
            }
        } catch (e: Exception) {
            null
        }
    }
}

actual fun getFileSharer(): FileSharer = AndroidFileSharer()
