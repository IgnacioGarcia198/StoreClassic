package com.garcia.ignacio.storeclassic.network.models

import com.garcia.ignacio.storeclassic.common.buildconfig.BuildConfig
import com.garcia.ignacio.storeclassic.data.exceptions.StoreException
import com.garcia.ignacio.storeclassic.domain.models.Discount
import kotlinx.serialization.Serializable

@Serializable
data class NetworkDiscount(
    val type: String,
    val productCode: String?,
    val params: List<Double>,
) {
    fun toDomain(): Discount = when (type) {
        Discount.BuyInBulk.TYPE ->
            Discount.BuyInBulk(
                productCode = productCode.orEmpty(),
                minimumBought = params.getOrThrowInDebug(0, 0.0).toInt(),
                discountPercent = params.getOrThrowInDebug(1, 0.0)
            )

        Discount.XForY.TYPE ->
            Discount.XForY(
                productCode = productCode.orEmpty(),
                productsBought = params.getOrThrowInDebug(0, 0.0).toInt(),
                productsPaid = params.getOrThrowInDebug(1, 0.0).toInt()
            )

        else ->
            Discount.Unimplemented(type, productCode.orEmpty(), params)
    }
}

private fun <T> List<T>.getOrThrowInDebug(position: Int, default: T): T =
    getOrNull(position) ?: if (BuildConfig.DEBUG)
        throw StoreException.Misusing(
            "Position $position is not available in this Discount params, wrong format came from network"
        )
    else default