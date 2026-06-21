package com.mrcoder20.portx

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.mrcoder20.portx.data.local.AppDatabase
import java.io.File

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual fun createDatabaseDriver(passphrase: String?): SqlDriver {
    val userHome = System.getProperty("user.home")
    val appDir = File(userHome, ".portx")
    if (!appDir.exists()) {
        appDir.mkdirs()
    }
    val databaseFile = File(appDir, "portx.db")
    val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:${databaseFile.absolutePath}")
    try {
        AppDatabase.Schema.create(driver)
    } catch (e: Exception) {
        // Schema exists
    }
    return driver
}
