package com.mtzdev.mywheatherapp

import android.app.Application
import com.mtzdev.mywheatherapp.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MyWeatherApp: Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@MyWeatherApp)
            modules(appModule)
        }
    }
}