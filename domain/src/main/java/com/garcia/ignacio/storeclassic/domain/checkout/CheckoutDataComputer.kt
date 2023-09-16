package com.garcia.ignacio.storeclassic.domain.checkout

import com.garcia.ignacio.storeclassic.domain.models.CheckoutRow
import com.garcia.ignacio.storeclassic.domain.models.DiscountedProduct
import com.garcia.ignacio.storeclassic.domain.models.Product

interface CheckoutDataComputer {
    suspend fun computeCheckoutData(
        cart: List<Product>,
        discountedProducts: List<DiscountedProduct>,
    ): List<CheckoutRow>
}