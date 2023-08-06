package com.garcia.ignacio.storeclassic.network.models

import com.garcia.ignacio.storeclassic.domain.models.Discount
import kotlinx.serialization.Serializable

@Serializable
data class NetworkDiscount(
    val type: String,
    val productCode: String?,
    val params: List<Double>
) {
    fun toDomain(): Discount = when (type) {
        Discount.BuyInBulk::class.simpleName ->
            Discount.BuyInBulk(
                productCode = productCode!!,
                minimumBought = params.first().toInt(),
                discountPercent = params[1]
            )

        Discount.XForY::class.simpleName -> {
            Discount.XForY(
                productCode = productCode!!,
                productsBought = params.first().toInt(),
                productsPaid = params[1].toInt()
            )
        }

        else -> throw NotImplementedError(type)
    }
}