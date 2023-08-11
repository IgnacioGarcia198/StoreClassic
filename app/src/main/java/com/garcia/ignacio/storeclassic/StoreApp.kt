package com.garcia.ignacio.storeclassic

import android.app.Application
import com.garcia.ignacio.storeclassic.network.update.StoreDataUpdater
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class StoreApp: Application() {
    @Inject
    lateinit var storeDataUpdater: StoreDataUpdater

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        storeDataUpdater.initialize()
    }
}