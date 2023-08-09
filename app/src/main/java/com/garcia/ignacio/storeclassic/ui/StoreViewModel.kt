package com.garcia.ignacio.storeclassic.ui

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.garcia.ignacio.storeclassic.common.ResultList
import com.garcia.ignacio.storeclassic.data.exceptions.ErrorType
import com.garcia.ignacio.storeclassic.data.repository.DiscountsRepository
import com.garcia.ignacio.storeclassic.data.repository.ProductsRepository
import com.garcia.ignacio.storeclassic.domain.models.Discount
import com.garcia.ignacio.storeclassic.domain.models.Product
import com.garcia.ignacio.storeclassic.ui.checkout.CheckoutRow
import com.garcia.ignacio.storeclassic.ui.checkout.DiscountedCheckoutRow
import com.garcia.ignacio.storeclassic.ui.checkout.NonDiscountedCheckoutRow
import com.garcia.ignacio.storeclassic.ui.discountlist.DiscountedProduct
import com.garcia.ignacio.storeclassic.ui.exceptions.ErrorHandler
import com.garcia.ignacio.storeclassic.ui.exceptions.ErrorReporter
import com.garcia.ignacio.storeclassic.ui.exceptions.ReportableError
import com.garcia.ignacio.storeclassic.ui.extensions.expressAsString
import com.garcia.ignacio.storeclassic.ui.livedata.Event
import com.garcia.ignacio.storeclassic.ui.model.AddToCart
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

private const val ERROR_REPORT_ITEM_SEPARATOR = "\n====================\n"
private const val ERROR_REPORT_HEADER = "ERROR REPORT\n\n"

@HiltViewModel
class StoreViewModel @Inject constructor(
    private val productsRepository: ProductsRepository,
    private val discountsRepository: DiscountsRepository,
    private val errorHandler: ErrorHandler,
    private val context: Application
) : ViewModel(), ErrorReporter {
    private val state = MutableLiveData<State>(State.Loading)
    fun getState(): LiveData<State> = state
    var pendingAddToCart: AddToCart? = null
        private set
    private val effect = MutableLiveData<Event<Effect>>(Event(Effect.Idle))
    fun getEffect(): LiveData<Event<Effect>> = effect

    private var discounts = emptyList<Discount>()
    private val cart = mutableListOf<Product>()

    fun getCheckoutRows(): LiveData<List<CheckoutRow>> {
        val rows = mutableListOf<CheckoutRow>()
        cart.groupBy { it.code }.values.forEach { productGroup ->
            val discount = discounts.find {
                it.productCode == productGroup.first().code
            } ?: let {
                rows.add(NonDiscountedCheckoutRow(productGroup, productGroup.sumOf { it.price }))
                return@forEach
            }
            val (applicable, nonApplicable) = discount.partitionApplicableProducts(productGroup)
            val discountedAmount = discount.apply(applicable)
            val discountedPercent =
                if (applicable.isEmpty()) 0.0 else discountedAmount / applicable.sumOf { it.price } * 100
            rows.add(
                DiscountedCheckoutRow(applicable, discount, discountedAmount, discountedPercent)
            )
            rows.add(
                NonDiscountedCheckoutRow(nonApplicable, nonApplicable.sumOf { it.price })
            )
        }
        return MutableLiveData(rows)
    }

    val discountedProducts: LiveData<List<DiscountedProduct>> = state.map { state ->
        when (state) {
            is State.Ready ->
                state.products.mapNotNull { product ->
                    discounts.find { discount ->
                        discount.isApplicableTo(product)
                    }?.let {
                        DiscountedProduct(product.code, product.name, it.expressAsString(context))
                    }
                }

            else -> emptyList()
        }
    }

    init {
        errorHandler.errorReporter = this
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
        pendingAddToCart?.let { addToCart ->
            repeat(addToCart.quantity) {
                cart.add(addToCart.product)
            }
            effect.value = Event(Effect.AddToCartConfirmed(addToCart))
            pendingAddToCart = null
        }
    }

    fun hasDiscounts(product: Product): Boolean = discounts.any { it.isApplicableTo(product) }

    override fun reportErrors(errors: List<ReportableError>) {
        val message = errors.joinToString("\n") { "- ${it.errorMessage}" }
        val report = errors.joinToString(
            ERROR_REPORT_ITEM_SEPARATOR,
            prefix = ERROR_REPORT_HEADER // TODO: Here we can add info on the device and OS
        ) { it.reportMessage }
        val reportableError = ReportableError(message, report)
        effect.value = Event(Effect.ReportErrors(reportableError))
    }

    fun displayDiscounts(product: Product) {
        effect.value = Event(Effect.DisplayDiscounts(product))
    }

    sealed interface Effect {
        object Idle : Effect
        object AddToCartConfirmation : Effect
        data class AddToCartConfirmed(val addToCart: AddToCart) : Effect
        data class ReportErrors(val compoundError: ReportableError) : Effect
        data class DisplayDiscounts(val product: Product) : Effect
    }

    sealed interface State {
        object Loading : State
        data class Ready(val products: List<Product>) : State
    }
}