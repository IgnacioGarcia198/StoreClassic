package com.garcia.ignacio.storeclassic.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.garcia.ignacio.storeclassic.data.ProductsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoreViewModel @Inject constructor(
    private val repository: ProductsRepository
) : ViewModel() {
    init {
        viewModelScope.launch {
            repository.products.flowOn(Dispatchers.IO).collect {
                Log.e("PRODUCTS", "PRODUCTS: $it")
            }
        }
    }
}