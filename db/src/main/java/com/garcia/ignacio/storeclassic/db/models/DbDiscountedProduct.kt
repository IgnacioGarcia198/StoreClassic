package com.garcia.ignacio.storeclassic.db.models

import com.garcia.ignacio.storeclassic.domain.models.DiscountedProduct
import com.garcia.ignacio.storeclassic.domain.models.Product

data class DbDiscountedProduct(
    val productCode: String,
    val name: String,
    val price: Double,
    val discountType: String? = null,
    val discountParams: List<Double>? = null,
) {
    fun toDomain(): DiscountedProduct = DiscountedProduct(
        Product(productCode, name, price),
        if (discountType != null && discountParams != null) {
            DbDiscount(discountType, productCode, discountParams).toDomain()
        } else null
    )
}