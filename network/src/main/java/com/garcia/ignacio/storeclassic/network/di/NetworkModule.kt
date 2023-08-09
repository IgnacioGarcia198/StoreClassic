package com.garcia.ignacio.storeclassic.network.di

import android.app.Application
import com.garcia.ignacio.storeclassic.data.remote.ConnectivityMonitor
import com.garcia.ignacio.storeclassic.data.remote.StoreClient
import com.garcia.ignacio.storeclassic.network.client.CabifyStoreClient
import com.garcia.ignacio.storeclassic.network.monitor.StoreConnectivityMonitor
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface NetworkModule {
    @Binds
    @Singleton
    fun bindStoreClient(client: CabifyStoreClient): StoreClient

    companion object {
        @Provides
        fun provideConnectivityMonitor(application: Application): ConnectivityMonitor =
            StoreConnectivityMonitor(application)
    }
}