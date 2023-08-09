package com.garcia.ignacio.storeclassic.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
import com.garcia.ignacio.storeclassic.ui.checkout.TotalCheckoutRow
import com.garcia.ignacio.storeclassic.ui.discountlist.DiscountedProduct
import com.garcia.ignacio.storeclassic.ui.exceptions.ErrorHandler
import com.garcia.ignacio.storeclassic.ui.exceptions.ErrorReporter
import com.garcia.ignacio.storeclassic.ui.exceptions.ReportableError
import com.garcia.ignacio.storeclassic.ui.livedata.Event
import com.garcia.ignacio.storeclassic.ui.model.AddToCart
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val ERROR_REPORT_ITEM_SEPARATOR = "\n====================\n"
private const val ERROR_REPORT_HEADER = "ERROR REPORT\n\n"

@HiltViewModel
class StoreViewModel @Inject constructor(
    private val productsRepository: ProductsRepository,
    private val discountsRepository: DiscountsRepository,
    private val errorHandler: ErrorHandler,
) : ViewModel(), ErrorReporter {
    private val state = MutableLiveData<State>(State.Loading)
    fun getState(): LiveData<State> = state
    var pendingAddToCart: AddToCart? = null
        private set
    private val effect = MutableLiveData<Event<Effect>>(Event(Effect.Idle))
    fun getEffect(): LiveData<Event<Effect>> = effect

    private val discounts: MutableLiveData<List<Discount>> = MutableLiveData(emptyList())
    private val cart: MutableLiveData<List<Product>> = MutableLiveData(emptyList())

    val discountedProducts: LiveData<List<DiscountedProduct>>
        get() {
            val mediator = MediatorLiveData<List<DiscountedProduct>>(emptyList())
            var productsState: State = State.Loading
            var discountsList: List<Discount> = emptyList()

            fun updateDiscountedProducts() {
                viewModelScope.launch {
                    findDiscountedProducts(productsState, discountsList).also {
                        mediator.value = it
                    }
                }
            }

            mediator.addSource(state) {
                productsState = it
                updateDiscountedProducts()
            }
            mediator.addSource(discounts) {
                discountsList = it
                updateDiscountedProducts()
            }
            return mediator
        }

    val checkoutData: LiveData<List<CheckoutRow>>
        get() {
            val mediator = MediatorLiveData(emptyList<CheckoutRow>())
            var discountsList: List<Discount> = emptyList()
            var cartList: List<Product> = emptyList()

            fun computeCheckoutData() {
                viewModelScope.launch {
                    computeCheckoutRows(cartList, discountsList)
                        .also { mediator.value = it }
                }
            }

            mediator.addSource(cart) {
                cartList = it
                computeCheckoutData()
            }
            mediator.addSource(discounts) {
                discountsList = it
                computeCheckoutData()
            }
            return mediator
        }

    fun clearCart() {
        cart.value = mutableListOf()
    }

    private suspend fun computeCheckoutRows(
        cart: List<Product>,
        discounts: List<Discount>,
    ): List<CheckoutRow> = withContext(Dispatchers.Default) {
        val discountedRows = mutableListOf<CheckoutRow>()
        val nonDiscountedRows = mutableListOf<CheckoutRow>()
        cart.groupBy { it.code }.values.forEach { productGroup ->
            val discount = discounts.find {
                it.productCode == productGroup.first().code
            } ?: let {
                nonDiscountedRows.add(
                    NonDiscountedCheckoutRow(
                        productGroup,
                        productGroup.sumOf { it.price })
                )
                return@forEach
            }
            val (applicable, nonApplicable) = discount.partitionApplicableProducts(productGroup)
            if (applicable.isNotEmpty()) {
                val discountedAmount = discount.apply(applicable)
                val discountedPercent = (1 - discountedAmount / applicable.sumOf { it.price }) * 100
                discountedRows.add(
                    DiscountedCheckoutRow(applicable, discount, discountedAmount, discountedPercent)
                )
            }
            if (nonApplicable.isNotEmpty()) {
                nonDiscountedRows.add(
                    NonDiscountedCheckoutRow(nonApplicable, nonApplicable.sumOf { it.price })
                )
            }
        }
        val checkoutRows = discountedRows + nonDiscountedRows
        return@withContext if (checkoutRows.isEmpty()) checkoutRows
        else {
            val totalAmount = checkoutRows.sumOf { it.amount }
            val originalAmount = cart.sumOf { it.price }
            val totalRow = TotalCheckoutRow(
                quantity = cart.size,
                amount = totalAmount,
                discountedPercent = (1 - totalAmount / originalAmount) * 100
            )
            checkoutRows + totalRow
        }
    }

    private suspend fun findDiscountedProducts(
        state: State,
        discounts: List<Discount>
    ): List<DiscountedProduct> = withContext(Dispatchers.Default) {
        when (state) {
            is State.Ready ->
                state.products.mapNotNull { product ->
                    discounts.find { discount ->
                        discount.isApplicableTo(product)
                    }?.let {
                        DiscountedProduct(product, it)
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
            discounts.value = result.result
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
            val toAdd = (1..addToCart.quantity).map { addToCart.product }
            cart.value = cart.value!! + toAdd
            effect.value = Event(Effect.AddToCartConfirmed(addToCart))
            pendingAddToCart = null
        }
    }

    fun hasDiscounts(product: Product): Boolean =
        discounts.value!!.any { it.isApplicableTo(product) }

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