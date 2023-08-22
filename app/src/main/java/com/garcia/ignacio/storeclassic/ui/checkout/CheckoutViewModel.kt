package com.garcia.ignacio.storeclassic.ui.checkout

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.garcia.ignacio.storeclassic.data.repository.DiscountedProductsRepository
import com.garcia.ignacio.storeclassic.ui.model.ListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val discountedProductsRepository: DiscountedProductsRepository,
    private val computer: CheckoutDataComputer,
) : ViewModel() {
    private val cart get() = discountedProductsRepository.cart

    private val checkoutState = MutableLiveData<ListState<CheckoutRow>>(ListState.Loading)
    fun getCheckoutState(): LiveData<ListState<CheckoutRow>> = checkoutState
    private val cartFlow = MutableStateFlow(cart.toList())

    init {
        initializeCheckoutData()
    }

    fun clearCart() {
        cart.clear()
        cartFlow.value = emptyList()
    }

    private fun initializeCheckoutData() {
        cartFlow.flatMapLatest { cart ->
            when {
                cart.isNotEmpty() -> {
                    discountedProductsRepository.findDiscountedProducts(
                        cart.map { it.code }.toSet()
                    ).map { list ->
                        computer.computeCheckoutData(cart, list)
                    }
                }

                else -> flowOf(emptyList())
            }.onEach {
                checkoutState.value = ListState.Ready(it)
            }
        }.launchIn(viewModelScope)
    }
}