package com.garcia.ignacio.storeclassic.db.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.garcia.ignacio.storeclassic.domain.models.Product

private const val PRODUCTS_TABLE_NAME = "products"

@Entity(tableName = PRODUCTS_TABLE_NAME)
data class DbProduct(
    @PrimaryKey val code: String,
    val name: String,
    val price: Double,
) {
    fun toDomain(): Product = Product(code, name, price)
}

fun Product.toDbProduct(): DbProduct = DbProduct(code, name, price)