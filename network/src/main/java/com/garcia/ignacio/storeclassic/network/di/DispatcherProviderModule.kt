package com.garcia.ignacio.storeclassic.network.di

import com.garcia.ignacio.storeclassic.testing.DefaultDispatcherProvider
import com.garcia.ignacio.storeclassic.testing.DispatcherProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface DispatcherProviderModule {
    @Binds
    fun bindDispatcherProvider(dispatcherProvider: DefaultDispatcherProvider): DispatcherProvider
}