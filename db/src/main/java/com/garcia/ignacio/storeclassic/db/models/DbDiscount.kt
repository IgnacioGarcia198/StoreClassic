package com.garcia.ignacio.storeclassic.db.models

import androidx.room.Entity
import com.garcia.ignacio.storeclassic.domain.models.Discount

private const val DISCOUNTS_TABLE_NAME = "discounts"

@Entity(tableName = DISCOUNTS_TABLE_NAME, primaryKeys = ["type", "productCode"])
data class DbDiscount(
    val type: String,
    val productCode: String,
    val params: List<Double>,
) {
    fun toDomain(): Discount = when (type) {
        Discount.BuyInBulk.TYPE ->
            Discount.BuyInBulk(
                productCode = productCode,
                minimumBought = params.getOrElse(0) { 0.0 }.toInt(),
                discountPercent = params.getOrElse(1) { 0.0 }
            )

        Discount.XForY.TYPE ->
            Discount.XForY(
                productCode = productCode,
                productsBought = params.firstOrNull()?.toInt() ?: 0,
                productsPaid = params.getOrElse(1) { 0.0 }.toInt()
            )

        else ->
            Discount.Unimplemented(type, productCode, params)
    }
}

fun Discount.toDbDiscount(): DbDiscount = when (this) {
    is Discount.BuyInBulk ->
        DbDiscount(
            type = Discount.BuyInBulk.TYPE,
            productCode = productCode,
            params = listOf(minimumBought.toDouble(), discountPercent)
        )

    is Discount.XForY ->
        DbDiscount(
            type = Discount.XForY.TYPE,
            productCode = productCode,
            params = listOf(productsBought.toDouble(), productsPaid.toDouble())
        )

    is Discount.Unimplemented ->
        DbDiscount(
            type = Discount.Unimplemented.TYPE,
            productCode = productCode,
            params = params
        )
}
