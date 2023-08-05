package com.garcia.ignacio.storeclassic.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.garcia.ignacio.storeclassic.data.ProductsRepository
import com.garcia.ignacio.storeclassic.domain.models.Product
import com.garcia.ignacio.storeclassic.ui.livedata.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class StoreViewModel @Inject constructor(
    private val repository: ProductsRepository
) : ViewModel() {
    private val state = MutableLiveData<State>(State.Loading)
    fun getState(): LiveData<State> = state
    var pendingAddToCart: AddToCart? = null
        private set
    private val effect = MutableLiveData<Event<Effect>>(Event(Effect.Idle))
    fun getEffect(): LiveData<Event<Effect>> = effect

    init {
        getRepositoryProducts()
    }

    private fun getRepositoryProducts() {
        repository.products.flowOn(
            Dispatchers.IO
        ).onEach { result ->
            state.value = State.Ready(result.result)
        }.launchIn(viewModelScope)
    }

    fun pendingAddToCart(product: Product, quantity: Int) {
        pendingAddToCart = AddToCart(product, quantity)
        effect.value = Event(Effect.AddToCartConfirmation)
    }

    sealed interface Effect {
        object Idle : Effect
        object AddToCartConfirmation : Effect
    }

    sealed interface State {
        object Loading : State
        data class Ready(val products: List<Product>) : State
    }
}