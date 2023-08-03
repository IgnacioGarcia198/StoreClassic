package com.garcia.ignacio.storeclassic.db.models

import androidx.room.Entity
import androidx.room.PrimaryKey

private const val PRODUCTS_TABLE_NAME = "products"

@Entity(tableName = PRODUCTS_TABLE_NAME)
data class DbProduct(
    @PrimaryKey val code: String,
    val name: String,
    val price: Double,
)