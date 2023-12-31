package com.garcia.ignacio.storeclassic.ui.productlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.garcia.ignacio.storeclassic.data.repository.DiscountedProductsRepository
import com.garcia.ignacio.storeclassic.domain.models.DiscountedProduct
import com.garcia.ignacio.storeclassic.domain.models.Product
import com.garcia.ignacio.storeclassic.ui.livedata.Event
import com.garcia.ignacio.storeclassic.ui.model.AddToCart
import com.garcia.ignacio.storeclassic.ui.model.ListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val discountedProductsRepository: DiscountedProductsRepository,
    private val cart: MutableList<Product>,
) : ViewModel() {
    private val productsState = MutableLiveData<ListState<DiscountedProduct>>(ListState.Loading)
    fun getProductsState(): LiveData<ListState<DiscountedProduct>> = productsState

    var pendingAddToCart: AddToCart? = null
        private set
    private val productsEffect = MutableLiveData<Event<ProductsEffect>>(Event(ProductsEffect.Idle))
    fun getProductsEffect(): LiveData<Event<ProductsEffect>> = productsEffect

    init {
        initializeAllProductsWithDiscountsIfAny()
    }

    fun pendingAddToCart(product: Product, quantity: Int) {
        pendingAddToCart = AddToCart(product, quantity)
        productsEffect.value = Event(ProductsEffect.AddToCartConfirmation)
    }

    fun pendingAddToCartCancelled() {
        pendingAddToCart = null
    }

    fun pendingAddToCartConfirmed() {
        pendingAddToCart?.let { addToCart ->
            val toAdd = (1..addToCart.quantity).map { addToCart.product }
            cart.addAll(toAdd)
            productsEffect.value = Event(ProductsEffect.AddToCartConfirmed(addToCart))
            pendingAddToCart = null
        }
    }

    fun displayDiscounts(product: Product) {
        productsEffect.value = Event(ProductsEffect.DisplayDiscounts(product))
    }

    fun goToCheckout() {
        productsEffect.value = Event(ProductsEffect.Checkout)
    }

    private fun initializeAllProductsWithDiscountsIfAny() {
        discountedProductsRepository.getAllProductsWithDiscountsIfAny().onEach {
            productsState.value = ListState.Ready(it)
        }.launchIn(viewModelScope)
    }
}