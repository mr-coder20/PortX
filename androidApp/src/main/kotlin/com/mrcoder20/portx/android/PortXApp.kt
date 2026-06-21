package com.mrcoder20.portx.android

import android.app.Application
import com.mrcoder20.portx.appContext
import com.mrcoder20.portx.di.initKoin
import org.koin.android.ext.koin.androidContext

class PortXApp : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = this
        initKoin {
            androidContext(this@PortXApp)
        }
    }
}
