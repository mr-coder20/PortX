package com.mrcoder20.portx

import android.os.Build
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.mrcoder20.portx.data.local.AppDatabase

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

lateinit var appContext: android.content.Context

actual fun createDatabaseDriver(passphrase: String?): SqlDriver {
    return AndroidSqliteDriver(AppDatabase.Schema, appContext, "portx.db")
}