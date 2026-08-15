package com.chumian.shizlite

import android.app.Application
import android.content.Context

class ShizLiteApp : Application() {
    companion object {
        lateinit var instance: ShizLiteApp
            private set
        val appContext: Context get() = instance.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
