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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoreViewModel @Inject constructor(
    private val repository: ProductsRepository
) : ViewModel() {
    private val products = MutableLiveData(emptyList<Product>())
    fun getProducts(): LiveData<List<Product>> = products

    init {
        viewModelScope.launch {
            repository.products.flowOn(Dispatchers.IO).collect { result ->
                products.value = result.result
            }
        }
    }
}