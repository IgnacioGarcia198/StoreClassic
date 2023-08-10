package com.garcia.ignacio.storeclassic.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.garcia.ignacio.storeclassic.common.ResultList
import com.garcia.ignacio.storeclassic.data.exceptions.ErrorType
import com.garcia.ignacio.storeclassic.data.remote.ConnectivityMonitor
import com.garcia.ignacio.storeclassic.data.repository.DiscountedProductsRepository
import com.garcia.ignacio.storeclassic.data.repository.DiscountsRepository
import com.garcia.ignacio.storeclassic.data.repository.ProductsRepository
import com.garcia.ignacio.storeclassic.domain.models.Discount
import com.garcia.ignacio.storeclassic.domain.models.DiscountedProduct
import com.garcia.ignacio.storeclassic.domain.models.Product
import com.garcia.ignacio.storeclassic.ui.checkout.CheckoutRow
import com.garcia.ignacio.storeclassic.ui.exceptions.ErrorHandler
import com.garcia.ignacio.storeclassic.ui.exceptions.ErrorReporter
import com.garcia.ignacio.storeclassic.ui.exceptions.ReportableError
import com.garcia.ignacio.storeclassic.ui.livedata.Event
import com.garcia.ignacio.storeclassic.ui.model.AddToCart
import com.garcia.ignacio.storeclassic.ui.productlist.AppEffect
import com.garcia.ignacio.storeclassic.ui.productlist.ProductsEffect
import com.garcia.ignacio.storeclassic.ui.productlist.ProductsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
    private val discountedProductsRepository: DiscountedProductsRepository,
    private val errorHandler: ErrorHandler,
    private val helper: StoreViewModelHelper,
    private val connectivityMonitor: ConnectivityMonitor,
) : ViewModel(), ErrorReporter {
    private val productsState = MutableLiveData<ProductsState>(ProductsState.Loading)
    fun getProductsState(): LiveData<ProductsState> = productsState
    var pendingAddToCart: AddToCart? = null
        private set
    private val productsEffect = MutableLiveData<Event<ProductsEffect>>(Event(ProductsEffect.Idle))
    private val appEffect = MutableLiveData<Event<AppEffect>>(Event(AppEffect.Idle))
    fun getProductsEffect(): LiveData<Event<ProductsEffect>> = productsEffect
    fun getAppEffect(): LiveData<Event<AppEffect>> = appEffect

    private val discounts: MutableLiveData<List<Discount>> = MutableLiveData(emptyList())
    private val cart: MutableLiveData<List<Product>> = MutableLiveData(emptyList())

    val checkoutData: LiveData<List<CheckoutRow>> by lazy {
        helper.getCheckoutData(cart, discounts, viewModelScope)
    }
    private var isConnectionAvailable = true
    private var updateJob: Job? = null

    private val discountsForCurrentProduct: MutableLiveData<List<DiscountedProduct>> =
        MutableLiveData(emptyList())

    fun getDiscountsForCurrentProduct(): LiveData<List<DiscountedProduct>> =
        discountsForCurrentProduct

    fun computeDiscountsForProduct(productCode: String? = null) {
        discountedProductsRepository.findDiscountedProducts(
            productCode?.let { setOf(productCode) } ?: emptySet()
        ).onEach { result ->
            discountsForCurrentProduct.value = result.result
            errorHandler.handleErrors(result.errors, ErrorType.DISCOUNT)
        }.launchIn(viewModelScope)
    }


    fun clearCart() {
        cart.value = mutableListOf()
    }

    init {
        errorHandler.errorReporter = this
        updateDataFromNetwork()
        startMonitoringNetworkConnection()
    }

    private fun updateDataFromNetwork() {
        productsState.value = ProductsState.Loading
        updateJob?.cancel()
        updateJob = getRepositoryDiscounts().combine(
            getRepositoryProducts()
        ) { _, _ -> }.launchIn(viewModelScope)
    }

    private fun startMonitoringNetworkConnection() {
        connectivityMonitor.isNetworkConnectedFlow
            .onEach { connectionAvailable ->
                if (!isConnectionAvailable && connectionAvailable) {
                    appEffect.value = Event(AppEffect.ConnectionRestored)
                    updateDataFromNetwork()
                }
                isConnectionAvailable = connectionAvailable
            }.launchIn(viewModelScope)
    }

    private fun getRepositoryProducts(): Flow<ResultList<List<Product>>> =
        productsRepository.products.onEach { result ->
            productsState.value = ProductsState.Ready(result.result)
            errorHandler.handleErrors(result.errors, ErrorType.PRODUCT)
        }

    private fun getRepositoryDiscounts(): Flow<ResultList<List<Discount>>> =
        discountsRepository.discounts.onEach { result ->
            discounts.value = result.result
            errorHandler.handleErrors(result.errors, ErrorType.DISCOUNT)
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
            cart.value = cart.value!! + toAdd
            productsEffect.value = Event(ProductsEffect.AddToCartConfirmed(addToCart))
            pendingAddToCart = null
        }
    }

    fun hasDiscounts(product: Product): Boolean =
        discounts.value!!.any { it.isApplicableTo(product) }

    override fun reportErrors(errors: Set<ReportableError>) {
        val message = errors.joinToString("\n") { "- ${it.errorMessage}" }
        val report = errors.joinToString(
            ERROR_REPORT_ITEM_SEPARATOR,
            prefix = ERROR_REPORT_HEADER // TODO: Here we can add info on the device and OS
        ) { it.reportMessage }
        val reportableError = ReportableError(message, report)
        appEffect.value = Event(AppEffect.ReportErrors(reportableError))
    }

    fun displayDiscounts(product: Product) {
        productsEffect.value = Event(ProductsEffect.DisplayDiscounts(product))
    }

    fun goToCheckout() {
        productsEffect.value = Event(ProductsEffect.Checkout)
    }

}