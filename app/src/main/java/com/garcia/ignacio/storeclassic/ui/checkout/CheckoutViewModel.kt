package com.garcia.ignacio.storeclassic.ui.checkout

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.garcia.ignacio.storeclassic.data.repository.DiscountedProductsRepository
import com.garcia.ignacio.storeclassic.domain.models.Product
import com.garcia.ignacio.storeclassic.ui.CheckoutViewModelHelper
import com.garcia.ignacio.storeclassic.ui.model.ListState
import com.garcia.ignacio.storeclassic.ui.model.UiProduct
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val discountedProductsRepository: DiscountedProductsRepository,
    private val helper: CheckoutViewModelHelper,
) : ViewModel() {

    private val checkoutState = MutableLiveData<ListState<CheckoutRow>>(ListState.Loading)
    fun getCheckoutState(): LiveData<ListState<CheckoutRow>> = checkoutState
    private val cart = MutableStateFlow<List<Product>>(emptyList())

    fun initialize(cart: Array<UiProduct>) {
        this.cart.value = cart.map { it.toDomain() }
        initializeCheckoutData()
    }

    fun clearCart() {
        cart.value = emptyList()
    }

    private fun initializeCheckoutData() {
        cart.flatMapLatest { cart ->
            discountedProductsRepository.findDiscountedProducts(
                cart.map { it.code }.toSet()
            ).map { result ->
                result.map {
                    helper.computeCheckoutData(cart, it)
                }.getOrDefault(
                    emptyList()
                ).also {
                    checkoutState.value = ListState.Ready(it)
                }
            }
        }.launchIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}