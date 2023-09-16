package com.garcia.ignacio.storeclassic.ui.model

import android.content.Context
import com.garcia.ignacio.storeclassic.domain.models.DiscountedProduct
import com.garcia.ignacio.storeclassic.ui.extensions.expressAsString

data class UiDiscountedProduct(
    val code: String,
    val name: String,
    val price: Double,
    val discountString: String?,
)

fun DiscountedProduct.toUi(context: Context): UiDiscountedProduct = UiDiscountedProduct(product.code, product.name, product.price, discount?.expressAsString(context))