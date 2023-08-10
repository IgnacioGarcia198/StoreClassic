package com.garcia.ignacio.storeclassic.db.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.garcia.ignacio.storeclassic.db.dao.DiscountDao
import com.garcia.ignacio.storeclassic.db.dao.DiscountedProductDao
import com.garcia.ignacio.storeclassic.db.dao.ProductDao
import com.garcia.ignacio.storeclassic.db.models.DbDiscount
import com.garcia.ignacio.storeclassic.db.models.DbProduct

@Database(
    entities = [DbProduct::class, DbDiscount::class],
    version = 1
)
@TypeConverters(com.garcia.ignacio.storeclassic.db.typeconverter.DbTypeConverters::class)
abstract class StoreDatabase : RoomDatabase() {
    abstract val productDao: ProductDao
    abstract val discountDao: DiscountDao
    abstract val discountedProductDao: DiscountedProductDao
}