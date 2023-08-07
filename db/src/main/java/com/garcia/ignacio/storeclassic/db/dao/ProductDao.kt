package com.garcia.ignacio.storeclassic.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.garcia.ignacio.storeclassic.db.models.DbProduct
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products")
    fun getAll(): Flow<List<DbProduct>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(products: List<DbProduct>)
}