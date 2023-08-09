package com.garcia.ignacio.storeclassic.ui.extensions

import android.content.Context
import com.garcia.ignacio.storeclassic.BuildConfig
import com.garcia.ignacio.storeclassic.R
import com.garcia.ignacio.storeclassic.data.exceptions.StoreException
import com.garcia.ignacio.storeclassic.domain.models.Discount
import com.garcia.ignacio.storeclassic.ui.formatting.StoreFormatter

private val formatter = StoreFormatter()

fun Discount.expressAsString(context: Context): String =
    when (this) {
        is Discount.XForY ->
            context.getString(R.string.x_for_y_discount, productsBought, productsPaid)

        is Discount.BuyInBulk ->
            context.getString(
                R.string.buy_in_bulk_discount,
                formatter.formatPercent(discountPercent),
                minimumBought
            )

        is Discount.Unimplemented ->
            if (BuildConfig.DEBUG)
                throw StoreException.Misusing("Unimplemented discount not to be printed")
            else
                context.getString(R.string.unimplemented_discount_not_available) // Here one option would be to actually display the unimplemented discounts greyed out with "update your app" or similar.
    }