package com.garcia.ignacio.storeclassic.ui.checkout

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.garcia.ignacio.storeclassic.data.repository.DiscountedProductsRepository
import com.garcia.ignacio.storeclassic.domain.models.Product
import com.garcia.ignacio.storeclassic.ui.model.ListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val discountedProductsRepository: DiscountedProductsRepository,
    private val computer: CheckoutDataComputer,
    private val cart: MutableList<Product>,
) : ViewModel() {

    private val checkoutState = MutableLiveData<ListState<CheckoutRow>>(ListState.Loading)
    fun getCheckoutState(): LiveData<ListState<CheckoutRow>> = checkoutState
    private val cartFlow = MutableStateFlow<List<Product>>(emptyList()).also { it.value = cart }

    init {
        initializeCheckoutData()
    }

    fun clearCart() {
        cart.clear()
        cartFlow.value = cart
    }

    private fun initializeCheckoutData() {
        cartFlow.flatMapLatest { cart ->
            discountedProductsRepository.findDiscountedProducts(
                cart.map { it.code }.toSet()
            ).onEach { list ->
                computer.computeCheckoutData(cart, list).also {
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