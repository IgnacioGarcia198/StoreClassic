package com.garcia.ignacio.storeclassic.ui.model

import android.os.Parcelable
import com.garcia.ignacio.storeclassic.domain.models.Product
import kotlinx.parcelize.Parcelize

@Parcelize
data class UiProduct(
    val code: String,
    val name: String,
    val price: Double,
) : Parcelable {
    fun toDomain(): Product = Product(code, name, price)
}

fun Product.toUi(): UiProduct = UiProduct(code, name, price)