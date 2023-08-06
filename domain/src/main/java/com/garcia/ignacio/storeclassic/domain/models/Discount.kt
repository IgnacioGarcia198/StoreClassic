package com.garcia.ignacio.storeclassic.domain.models

private val ALL_PRODUCTS: String? = null

sealed class Discount {
    abstract val productCode: String?

    fun apply(products: List<Product>): Double = when (productCode) {
        ALL_PRODUCTS -> applyToAll(products)
        else -> products.filter {
            it.code == productCode
        }.let { applicableProducts ->
            if (applicableProducts.isNotEmpty()) applyDiscount(applicableProducts)
            else 0.0
        }
    }

    protected open fun applyToAll(products: List<Product>): Double {
        return products.groupBy { it.code }.values.sumOf { applyDiscount(it) }
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

        companion object {
            const val TYPE = "XForY"
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

        companion object {
            const val TYPE = "BuyInBulk"
        }
    }

    data class Unimplemented(
        val type: String,
        override val productCode: String?,
        val params: List<Double>,
    ) : Discount() {
        override fun applyDiscount(applicableProducts: List<Product>): Double {
            return applicableProducts.sumOf { it.price }
        }

        companion object {
            const val TYPE = "Unimplemented"
        }
    }
}