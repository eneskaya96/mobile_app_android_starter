package com.example.webviewapp

import android.app.Application
import com.appsflyer.AppsFlyerLib

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        /*
        AppsFlyerLib.getInstance().init( BuildConfig.APPSFLYER_DEV_KEY, null, this)
        AppsFlyerLib.getInstance().start(this)
        AppsFlyerLib.getInstance().setDebugLog(true)

         */
    }
}
