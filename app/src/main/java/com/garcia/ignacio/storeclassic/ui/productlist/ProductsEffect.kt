package com.garcia.ignacio.storeclassic.ui.productlist

import com.garcia.ignacio.storeclassic.domain.models.Product
import com.garcia.ignacio.storeclassic.ui.exceptions.ReportableError
import com.garcia.ignacio.storeclassic.ui.model.AddToCart

sealed interface ProductsEffect {
    object Idle : ProductsEffect
    object AddToCartConfirmation : ProductsEffect
    data class AddToCartConfirmed(val addToCart: AddToCart) : ProductsEffect
    data class ReportErrors(val compoundError: ReportableError) : ProductsEffect
    data class DisplayDiscounts(val product: Product) : ProductsEffect
    object Checkout : ProductsEffect
}