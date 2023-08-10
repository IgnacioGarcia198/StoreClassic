package com.garcia.ignacio.storeclassic.db.models

import com.garcia.ignacio.storeclassic.domain.models.DiscountedProduct
import com.garcia.ignacio.storeclassic.domain.models.Product

data class DbDiscountedProduct(
    val productCode: String,
    val name: String,
    val price: Double,
    val discountType: String,
    val discountParams: List<Double>,
) {
    fun toDomain(): DiscountedProduct = DiscountedProduct(
        Product(productCode, name, price),
        DbDiscount(discountType, productCode, discountParams).toDomain()
    )
}