package com.garcia.ignacio.storeclassic.ui.discountlist

import com.garcia.ignacio.storeclassic.domain.models.Discount
import com.garcia.ignacio.storeclassic.domain.models.Product

data class DiscountedProduct constructor(
    val product: Product,
    val discount: Discount
)
