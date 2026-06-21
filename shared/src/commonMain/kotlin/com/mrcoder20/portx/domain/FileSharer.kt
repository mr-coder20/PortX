package com.mrcoder20.portx.domain

interface FileSharer {
    fun shareFile(content: String, fileName: String, mimeType: String = "application/json")
    fun downloadFile(content: String, fileName: String, mimeType: String = "application/json"): String?
}

expect fun getFileSharer(): FileSharer
