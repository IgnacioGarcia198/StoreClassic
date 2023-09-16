package com.garcia.ignacio.storeclassic.ui.discountlist

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.garcia.ignacio.storeclassic.data.repository.DiscountedProductsRepository
import com.garcia.ignacio.storeclassic.ui.model.ListState
import com.garcia.ignacio.storeclassic.ui.model.UiDiscountedProduct
import com.garcia.ignacio.storeclassic.ui.model.toUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class DiscountsViewModel @Inject constructor(
    private val discountedProductsRepository: DiscountedProductsRepository,
) : ViewModel() {

    private val discountsState = MutableLiveData<ListState<UiDiscountedProduct>>(ListState.Loading)
    fun getDiscountsState(): LiveData<ListState<UiDiscountedProduct>> = discountsState

    fun initialize(productCode: String?, context: Context) {
        initializeDiscountsForProduct(productCode, context)
    }

    private fun initializeDiscountsForProduct(productCode: String?, context: Context) {
        discountedProductsRepository.findDiscountedProducts(
            productCode?.let { setOf(productCode) } ?: emptySet()
        ).onEach {
            discountsState.value = ListState.Ready(it.map { list -> list.toUi(context) })
        }.launchIn(viewModelScope)
    }
}