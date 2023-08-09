package com.garcia.ignacio.storeclassic.ui

import androidx.lifecycle.MutableLiveData
import com.garcia.ignacio.storeclassic.domain.models.Discount
import com.garcia.ignacio.storeclassic.domain.models.Product
import com.garcia.ignacio.storeclassic.ui.checkout.CheckoutRow
import com.garcia.ignacio.storeclassic.ui.checkout.DiscountedCheckoutRow
import com.garcia.ignacio.storeclassic.ui.checkout.NonDiscountedCheckoutRow
import com.garcia.ignacio.storeclassic.ui.checkout.TotalCheckoutRow
import com.garcia.ignacio.storeclassic.ui.discountlist.DiscountedProduct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StoreViewModelHelper @Inject constructor() {

    fun updateDiscountedProducts(
        state: StoreViewModel.State,
        discounts: List<Discount>,
        scope: CoroutineScope,
        liveData: MutableLiveData<List<DiscountedProduct>>,
    ) {
        scope.launch {
            findDiscountedProducts(state, discounts).also {
                liveData.value = it
            }
        }
    }

    fun computeCheckoutData(
        cart: List<Product>,
        discounts: List<Discount>,
        scope: CoroutineScope,
        liveData: MutableLiveData<List<CheckoutRow>>,
    ) {
        scope.launch {
            computeCheckoutRows(cart, discounts)
                .also { liveData.value = it }
        }
    }

    private suspend fun computeCheckoutRows(
        cart: List<Product>,
        discounts: List<Discount>,
    ): List<CheckoutRow> = withContext(Dispatchers.Default) {
        val discountedRows = mutableListOf<CheckoutRow>()
        val nonDiscountedRows = mutableListOf<CheckoutRow>()
        cart.groupBy { it.code }.values.forEach { productGroup ->
            val discount = discounts.find {
                it.productCode == productGroup.first().code
            } ?: let {
                nonDiscountedRows.add(
                    NonDiscountedCheckoutRow(
                        productGroup,
                        productGroup.sumOf { it.price })
                )
                return@forEach
            }
            val (applicable, nonApplicable) = discount.partitionApplicableProducts(productGroup)
            if (applicable.isNotEmpty()) {
                val discountedAmount = discount.apply(applicable)
                val discountedPercent = (1 - discountedAmount / applicable.sumOf { it.price }) * 100
                discountedRows.add(
                    DiscountedCheckoutRow(applicable, discount, discountedAmount, discountedPercent)
                )
            }
            if (nonApplicable.isNotEmpty()) {
                nonDiscountedRows.add(
                    NonDiscountedCheckoutRow(nonApplicable, nonApplicable.sumOf { it.price })
                )
            }
        }
        val checkoutRows = discountedRows + nonDiscountedRows
        return@withContext if (checkoutRows.isEmpty()) checkoutRows
        else {
            val totalAmount = checkoutRows.sumOf { it.amount }
            val originalAmount = cart.sumOf { it.price }
            val totalRow = TotalCheckoutRow(
                quantity = cart.size,
                amount = totalAmount,
                discountedPercent = (1 - totalAmount / originalAmount) * 100
            )
            checkoutRows + totalRow
        }
    }

    private suspend fun findDiscountedProducts(
        state: StoreViewModel.State,
        discounts: List<Discount>
    ): List<DiscountedProduct> = withContext(Dispatchers.Default) {
        when (state) {
            is StoreViewModel.State.Ready ->
                state.products.mapNotNull { product ->
                    discounts.find { discount ->
                        discount.isApplicableTo(product)
                    }?.let {
                        DiscountedProduct(product, it)
                    }
                }

            else -> emptyList()
        }
    }
}