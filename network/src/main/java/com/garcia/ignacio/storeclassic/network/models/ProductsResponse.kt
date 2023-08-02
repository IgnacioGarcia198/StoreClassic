package com.garcia.ignacio.storeclassic.network.models

import kotlinx.serialization.Serializable

@Serializable
data class ProductsResponse(
    val products: List<NetworkProduct>
)