package com.garcia.ignacio.storeclassic.ui.model

import com.garcia.ignacio.storeclassic.domain.models.Product

data class AddToCart(
    val product: Product,
    val quantity: Int
)