package com.garcia.ignacio.storeclassic.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.garcia.ignacio.storeclassic.common.ResultList
import com.garcia.ignacio.storeclassic.data.repository.DiscountsRepository
import com.garcia.ignacio.storeclassic.data.repository.ProductsRepository
import com.garcia.ignacio.storeclassic.domain.models.Discount
import com.garcia.ignacio.storeclassic.domain.models.Product
import com.garcia.ignacio.storeclassic.ui.exceptions.ErrorHandler
import com.garcia.ignacio.storeclassic.data.exceptions.ErrorType
import com.garcia.ignacio.storeclassic.ui.livedata.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class StoreViewModel @Inject constructor(
    private val productsRepository: ProductsRepository,
    private val discountsRepository: DiscountsRepository,
    private val errorHandler: ErrorHandler,
) : ViewModel() {
    private val state = MutableLiveData<State>(State.Loading)
    fun getState(): LiveData<State> = state
    var pendingAddToCart: AddToCart? = null
        private set
    private val effect = MutableLiveData<Event<Effect>>(Event(Effect.Idle))
    fun getEffect(): LiveData<Event<Effect>> = effect

    private var discounts = emptyList<Discount>()

    init {
        getRepositoryDiscounts().combine(
            getRepositoryProducts()
        ) { _, _ -> }.launchIn(viewModelScope)
    }

    private fun getRepositoryProducts(): Flow<ResultList<List<Product>>> =
        productsRepository.products.onEach { result ->
            state.value = State.Ready(result.result)
            errorHandler.handleErrors(result.errors, ErrorType.PRODUCT)
        }

    private fun getRepositoryDiscounts(): Flow<ResultList<List<Discount>>> =
        discountsRepository.discounts.onEach { result ->
            discounts = result.result
            errorHandler.handleErrors(result.errors, ErrorType.DISCOUNT)
        }

    fun pendingAddToCart(product: Product, quantity: Int) {
        pendingAddToCart = AddToCart(product, quantity)
        effect.value = Event(Effect.Idle)
        effect.value = Event(Effect.AddToCartConfirmation)
    }

    fun pendingAddToCartCancelled() {
        pendingAddToCart = null
    }

    fun pendingAddToCartConfirmed() {
        pendingAddToCart?.let {
            effect.value = Event(Effect.AddToCartConfirmed(it))
            pendingAddToCart = null
        }
    }

    sealed interface Effect {
        object Idle : Effect
        object AddToCartConfirmation : Effect
        data class AddToCartConfirmed(val addToCart: AddToCart) : Effect
    }

    sealed interface State {
        object Loading : State
        data class Ready(val products: List<Product>) : State
    }
}