package com.garcia.ignacio.storeclassic.di

import com.garcia.ignacio.storeclassic.data.exceptions.ErrorHandler
import com.garcia.ignacio.storeclassic.exceptions.StoreErrorHandler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface ErrorHandlerModule {
    @Singleton
    @Binds
    fun bindErrorHandler(errorHandler: StoreErrorHandler): ErrorHandler
}