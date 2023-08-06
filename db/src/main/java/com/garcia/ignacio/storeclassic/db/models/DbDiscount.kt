package com.garcia.ignacio.storeclassic.db.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.garcia.ignacio.storeclassic.domain.models.Discount

private const val DISCOUNTS_TABLE_NAME = "discounts"

@Entity(tableName = DISCOUNTS_TABLE_NAME)
data class DbDiscount(
    val type: String,
    val productCode: String?,
    val params: List<Double>,
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
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

        else -> throw NotImplementedError("Misusing: $type is not implemented and should not be in the db!!")
    }
}

fun Discount.toDbDiscount(): DbDiscount = when (this) {
    is Discount.BuyInBulk -> DbDiscount(
        type = this::class.simpleName!!,
        productCode = productCode,
        params = listOf(minimumBought.toDouble(), discountPercent)
    )

    is Discount.XForY -> DbDiscount(
        type = this::class.simpleName!!,
        productCode = productCode,
        params = listOf(productsBought.toDouble(), productsPaid.toDouble())
    )
}
