package com.garcia.ignacio.storeclassic.network.models

import com.garcia.ignacio.storeclassic.data.exceptions.assertOnDebug
import com.garcia.ignacio.storeclassic.domain.models.Discount
import kotlinx.serialization.Serializable

@Serializable
data class NetworkDiscount(
    val type: String?,
    val productCode: String?,
    val params: List<Double>,
) {
    fun toDomain(): Discount = when (type) {
        Discount.BuyInBulk.TYPE -> {
            assertOnDebug("need 2 params") { params.size >= 2 }
            Discount.BuyInBulk(
                productCode = productCode.orEmpty(),
                minimumBought = params.getOrElse(0) { 0.0 }.toInt(),
                discountPercent = params.getOrElse(1) { 0.0 }
            )
        }

        Discount.XForY.TYPE -> {
            assertOnDebug("need 2 params") { params.size >= 2 }
            assertOnDebug("productsBought cannot be smaller than productsPaid") {
                params.first() > params[1]
            }
            Discount.XForY(
                productCode = productCode.orEmpty(),
                productsBought = params.firstOrNull()?.toInt() ?: 0,
                productsPaid = params.getOrElse(1) { 0.0 }.toInt()
            )
        }

        else ->
            Discount.Unimplemented(type.orEmpty(), productCode.orEmpty(), params)
    }
}