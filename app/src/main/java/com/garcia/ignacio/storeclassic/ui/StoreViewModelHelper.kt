package com.garcia.ignacio.storeclassic.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.garcia.ignacio.storeclassic.domain.models.Discount
import com.garcia.ignacio.storeclassic.domain.models.Product
import com.garcia.ignacio.storeclassic.ui.checkout.CheckoutRow
import com.garcia.ignacio.storeclassic.ui.checkout.DiscountedCheckoutRow
import com.garcia.ignacio.storeclassic.ui.checkout.NonDiscountedCheckoutRow
import com.garcia.ignacio.storeclassic.ui.checkout.TotalCheckoutRow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StoreViewModelHelper @Inject constructor() {

    fun getCheckoutData(
        cartLiveData: LiveData<List<Product>>,
        discountsLiveData: LiveData<List<Discount>>,
        scope: CoroutineScope,
    ): MediatorLiveData<List<CheckoutRow>> {
        val mediator = MediatorLiveData(emptyList<CheckoutRow>())
        var discounts: List<Discount> = emptyList()
        var cart: List<Product> = emptyList()

        mediator.addSource(cartLiveData) {
            cart = it
            computeCheckoutData(cart, discounts, scope, mediator)
        }
        mediator.addSource(discountsLiveData) {
            discounts = it
            computeCheckoutData(cart, discounts, scope, mediator)
        }
        return mediator
    }

    private fun computeCheckoutData(
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
}