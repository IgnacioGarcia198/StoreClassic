package com.garcia.ignacio.storeclassic.di

import com.garcia.ignacio.storeclassic.network.client.CabifyStoreClient
import com.garcia.ignacio.storeclassic.network.client.StoreClient
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface NetworkModule {
    @Binds
    fun bindStoreClient(client: CabifyStoreClient): StoreClient
}