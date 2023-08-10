package com.garcia.ignacio.storeclassic.ui.discountlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.garcia.ignacio.storeclassic.data.repository.DiscountedProductsRepository
import com.garcia.ignacio.storeclassic.domain.models.DiscountedProduct
import com.garcia.ignacio.storeclassic.ui.model.ListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DiscountsViewModel @Inject constructor(
    private val discountedProductsRepository: DiscountedProductsRepository,
) : ViewModel() {

    private val discountsState = MutableLiveData<ListState<DiscountedProduct>>(ListState.Loading)
    fun getDiscountsState(): LiveData<ListState<DiscountedProduct>> = discountsState

    fun initialize(productCode: String?) {
        initializeDiscountsForProduct(productCode)
    }

    private fun initializeDiscountsForProduct(productCode: String?) {
        discountedProductsRepository.findDiscountedProducts(
            productCode?.let { setOf(productCode) } ?: emptySet()
        ).map { result ->
            result.getOrDefault(
                emptyList()
            ).also {
                discountsState.value = ListState.Ready(it)
            }
        }.launchIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}