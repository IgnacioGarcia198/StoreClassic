package com.garcia.ignacio.storeclassic.domain.models

sealed class Discount {
    abstract val productCode: String

    fun apply(applicableProducts: List<Product>): Double {
        return if (applicableProducts.isEmpty()) 0.0 else applyDiscount(applicableProducts)
    }

    abstract fun partitionApplicableProducts(
        products: List<Product>
    ): Pair<List<Product>, List<Product>>

    protected abstract fun applyDiscount(applicableProducts: List<Product>): Double

    data class XForY(
        override val productCode: String,
        val productsBought: Int,
        val productsPaid: Int,
    ) : Discount() {
        override fun applyDiscount(applicableProducts: List<Product>): Double {
            val originalPrice = applicableProducts.first().price
            return applicableProducts.size * originalPrice * productsPaid / productsBought
        }

        override fun partitionApplicableProducts(
            products: List<Product>
        ): Pair<List<Product>, List<Product>> =
            products.partition { it.code == productCode }.let { (applicable, nonApplicable) ->
                val cannotApply = applicable.size % productsBought
                if (cannotApply > 0) {
                    val newNonApplicable = nonApplicable + applicable.takeLast(cannotApply)
                    applicable.dropLast(cannotApply) to newNonApplicable
                } else applicable to nonApplicable
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
            return applicableProducts.size * originalPrice * (1 - discountPercent / 100)
        }

        override fun partitionApplicableProducts(
            products: List<Product>
        ): Pair<List<Product>, List<Product>> =
            if (products.size >= minimumBought) products to emptyList()
            else emptyList<Product>() to products

        companion object {
            const val TYPE = "BuyInBulk"
        }
    }

    data class Unimplemented(
        val type: String,
        override val productCode: String,
        val params: List<Double>,
    ) : Discount() {
        override fun partitionApplicableProducts(
            products: List<Product>
        ): Pair<List<Product>, List<Product>> = emptyList<Product>() to products

        override fun applyDiscount(applicableProducts: List<Product>): Double {
            return applicableProducts.sumOf { it.price }
        }

        companion object {
            const val TYPE = "Unimplemented"
        }
    }
}