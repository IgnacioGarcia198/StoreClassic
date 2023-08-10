package com.garcia.ignacio.storeclassic.db.di

import android.app.Application
import androidx.room.Room
import com.garcia.ignacio.storeclassic.db.dao.DiscountDao
import com.garcia.ignacio.storeclassic.db.dao.DiscountedProductDao
import com.garcia.ignacio.storeclassic.db.dao.ProductDao
import com.garcia.ignacio.storeclassic.db.database.StoreDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val DATABASE_NAME = "storeDb.db"

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {
    @Singleton
    @Provides
    fun provideDatabase(application: Application): StoreDatabase {
        return Room.databaseBuilder(
            application,
            StoreDatabase::class.java, DATABASE_NAME
        ).build()
    }

    @Provides
    fun provideProductDao(database: StoreDatabase): ProductDao {
        return database.productDao
    }

    @Provides
    fun provideDiscountDao(database: StoreDatabase): DiscountDao {
        return database.discountDao
    }

    @Provides
    fun provideDiscountedProductDao(database: StoreDatabase): DiscountedProductDao {
        return database.discountedProductDao
    }
}