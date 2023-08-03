package com.garcia.ignacio.storeclassic.db.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.garcia.ignacio.storeclassic.db.dao.ProductDao
import com.garcia.ignacio.storeclassic.db.models.DbProduct

@Database(entities = [DbProduct::class], version = 1)
abstract class ProductDatabase : RoomDatabase() {
    abstract val productDao: ProductDao
}