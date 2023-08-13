package com.garcia.ignacio.storeclassic.network.di

import android.app.Application
import android.net.ConnectivityManager
import androidx.core.content.ContextCompat
import com.garcia.ignacio.storeclassic.data.remote.ConnectivityMonitor
import com.garcia.ignacio.storeclassic.data.remote.StoreClient
import com.garcia.ignacio.storeclassic.network.client.CabifyStoreClient
import com.garcia.ignacio.storeclassic.network.monitor.StoreConnectivityMonitor
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android
import javax.inject.Singleton

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface NetworkModule {
    @Binds
    @Singleton
    fun bindStoreClient(client: CabifyStoreClient): StoreClient

    @Binds
    fun bindConnectivityMonitor(monitor: StoreConnectivityMonitor): ConnectivityMonitor

    companion object {
        @Provides
        fun provideConnectivityManager(
            application: Application
        ): ConnectivityManager = ContextCompat.getSystemService(
            application, ConnectivityManager::class.java
        )!!

        @Provides
        fun provideHttpClientEngine(): HttpClientEngine = Android.create()

    }
}