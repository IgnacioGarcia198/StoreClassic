package com.garcia.ignacio.storeclassic.network.di

import com.garcia.ignacio.storeclassic.network.client.CabifyStoreClient
import com.garcia.ignacio.storeclassic.data.remote.StoreClient
import dagger.Binds
import dagger.Module
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
}