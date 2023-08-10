package com.garcia.ignacio.storeclassic.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.garcia.ignacio.storeclassic.db.models.DbDiscountedProduct
import kotlinx.coroutines.flow.Flow

@Dao
abstract class DiscountedProductDao {
    @Query(
        "SELECT products.code AS productCode, products.name, products.price, " +
                "discounts.type AS discountType, discounts.params AS discountParams " +
                "FROM discounts JOIN products ON discounts.productCode = products.code " +
                "WHERE productCode IN (:productCodes)"
    )
    abstract fun findDiscountedProducts(productCodes: Set<String>): Flow<List<DbDiscountedProduct>>

    @Query(
        "SELECT products.code AS productCode, products.name, products.price, " +
                "discounts.type AS discountType, discounts.params AS discountParams " +
                "FROM discounts JOIN products ON discounts.productCode = products.code"
    )
    abstract fun getAllDiscountedProducts(): Flow<List<DbDiscountedProduct>>

    fun getDiscountedProducts(productCodes: Set<String>): Flow<List<DbDiscountedProduct>> {
        return if (productCodes.isEmpty()) getAllDiscountedProducts()
        else findDiscountedProducts(productCodes)
    }
}