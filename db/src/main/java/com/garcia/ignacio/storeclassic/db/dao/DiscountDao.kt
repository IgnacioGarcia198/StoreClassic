package com.garcia.ignacio.storeclassic.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.garcia.ignacio.storeclassic.db.models.DbDiscount
import kotlinx.coroutines.flow.Flow

@Dao
interface DiscountDao {
    @Query("SELECT * FROM discounts")
    fun getAll(): Flow<List<DbDiscount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<DbDiscount>)
}