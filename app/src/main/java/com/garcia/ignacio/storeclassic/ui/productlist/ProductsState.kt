package com.garcia.ignacio.storeclassic.ui.productlist

import com.garcia.ignacio.storeclassic.domain.models.Product

sealed interface ProductsState {
    object Loading : ProductsState
    data class Ready(val products: List<Product>) : ProductsState
}