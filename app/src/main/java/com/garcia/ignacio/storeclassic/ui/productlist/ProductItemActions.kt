package com.garcia.ignacio.storeclassic.ui.productlist

import com.garcia.ignacio.storeclassic.domain.models.Product

interface ProductItemActions {
    fun productQuantitySelected(product: Product, quantity: Int)

    fun seeDiscounts(product: Product)
}