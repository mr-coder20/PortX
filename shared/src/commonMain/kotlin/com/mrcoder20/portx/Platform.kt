package com.mrcoder20.portx

import app.cash.sqldelight.db.SqlDriver

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun createDatabaseDriver(passphrase: String? = null): SqlDriver
