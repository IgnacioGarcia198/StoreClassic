package com.garcia.ignacio.storeclassic.domain.models

sealed class Discount {
    abstract val productCode: String?

    fun apply(products: List<Product>) {
        val applicableProducts = productCode?.let { code ->
            products.filter { it.code == code }
        } ?: products
        if (applicableProducts.isNotEmpty()) {
            applyDiscount(applicableProducts)
        }
    }


    protected abstract fun applyDiscount(applicableProducts: List<Product>): Double

    fun isApplicableTo(product: Product): Boolean =
        productCode == null || productCode == product.code

    data class XForY(
        override val productCode: String,
        val productsBought: Int,
        val productsPaid: Int,
    ) : Discount() {
        override fun applyDiscount(applicableProducts: List<Product>): Double {
            val originalPrice = applicableProducts.first().price
            val singleProducts = applicableProducts.size % productsBought
            val completeGroupProducts = applicableProducts.size - singleProducts
            return completeGroupProducts * originalPrice * productsPaid / productsBought +
                    singleProducts * originalPrice
        }
    }

    data class BuyInBulk(
        override val productCode: String,
        val minimumBought: Int,
        val discountPercent: Double,
    ) : Discount() {
        override fun applyDiscount(applicableProducts: List<Product>): Double {
            val originalPrice = applicableProducts.first().price
            val price = if (applicableProducts.size >= minimumBought) {
                originalPrice * (1 - discountPercent / 100)
            } else originalPrice
            return applicableProducts.size * price
        }
    }
}