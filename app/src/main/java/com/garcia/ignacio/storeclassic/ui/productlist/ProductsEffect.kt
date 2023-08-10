package com.garcia.ignacio.storeclassic.ui.productlist

import com.garcia.ignacio.storeclassic.domain.models.Product
import com.garcia.ignacio.storeclassic.ui.model.AddToCart
import com.garcia.ignacio.storeclassic.ui.model.UiProduct

sealed interface ProductsEffect {
    object Idle : ProductsEffect
    object AddToCartConfirmation : ProductsEffect
    data class AddToCartConfirmed(val addToCart: AddToCart) : ProductsEffect
    data class DisplayDiscounts(val product: Product) : ProductsEffect
    data class Checkout(val products: List<UiProduct>) : ProductsEffect
}