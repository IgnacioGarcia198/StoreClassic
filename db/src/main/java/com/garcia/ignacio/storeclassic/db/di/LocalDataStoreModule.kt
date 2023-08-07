package com.garcia.ignacio.storeclassic.db.di

import com.garcia.ignacio.storeclassic.data.local.DiscountsLocalDataStore
import com.garcia.ignacio.storeclassic.data.local.ProductsLocalDataStore
import com.garcia.ignacio.storeclassic.db.DiscountsStorage
import com.garcia.ignacio.storeclassic.db.ProductsStorage
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface LocalDataStoreModule {
    @Binds
    fun bindProductsLocalDataStore(
        storage: ProductsStorage
    ): ProductsLocalDataStore

    @Binds
    fun bindDiscountsLocalDataStore(
        storage: DiscountsStorage
    ): DiscountsLocalDataStore
}