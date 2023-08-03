package com.garcia.ignacio.storeclassic.db.di

import android.app.Application
import androidx.room.Room
import com.garcia.ignacio.storeclassic.db.dao.ProductDao
import com.garcia.ignacio.storeclassic.db.database.ProductDatabase
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
    fun provideDatabase(application: Application): ProductDatabase {
        return Room.databaseBuilder(
            application,
            ProductDatabase::class.java, DATABASE_NAME
        ).build()
    }

    @Provides
    fun provideProductDao(database: ProductDatabase): ProductDao {
        return database.productDao
    }
}