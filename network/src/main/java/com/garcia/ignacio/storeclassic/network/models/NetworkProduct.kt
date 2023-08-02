package com.garcia.ignacio.storeclassic.network.models

import kotlinx.serialization.Serializable

@Serializable
data class NetworkProduct(
    val code: String,
    val name: String,
    val price: Double
)