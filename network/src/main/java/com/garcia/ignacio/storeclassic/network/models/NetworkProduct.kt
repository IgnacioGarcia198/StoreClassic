package com.garcia.ignacio.storeclassic.network.models

import com.garcia.ignacio.storeclassic.domain.models.Product
import kotlinx.serialization.Serializable

@Serializable
data class NetworkProduct(
    val code: String,
    val name: String,
    val price: Double
) {
    fun toDomain(): Product = Product(code, name, price)
}