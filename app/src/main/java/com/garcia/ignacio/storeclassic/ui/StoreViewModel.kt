package com.garcia.ignacio.storeclassic.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.garcia.ignacio.storeclassic.data.ProductsRepository
import com.garcia.ignacio.storeclassic.domain.models.Product
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
    private val products = MutableLiveData(emptyList<Product>())
    fun getProducts(): LiveData<List<Product>> = products
    private var pendingAddToCart: AddToCart? = null

    init {
        getRepositoryProducts()
    }

    private fun getRepositoryProducts() {
        repository.products.flowOn(
            Dispatchers.IO
        ).onEach { result ->
            products.value = result.result
        }.launchIn(viewModelScope)
    }

    fun pendingAddToCart(product: Product, quantity: Int) {
        pendingAddToCart = AddToCart(product, quantity)
    }
}