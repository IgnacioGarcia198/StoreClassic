package com.garcia.ignacio.storeclassic.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.garcia.ignacio.storeclassic.data.exceptions.ErrorType
import com.garcia.ignacio.storeclassic.data.remote.ConnectivityMonitor
import com.garcia.ignacio.storeclassic.data.repository.DiscountedProductsRepository
import com.garcia.ignacio.storeclassic.data.repository.DiscountsRepository
import com.garcia.ignacio.storeclassic.data.repository.ProductsRepository
import com.garcia.ignacio.storeclassic.domain.models.DiscountedProduct
import com.garcia.ignacio.storeclassic.domain.models.Product
import com.garcia.ignacio.storeclassic.ui.checkout.CheckoutRow
import com.garcia.ignacio.storeclassic.ui.exceptions.ErrorHandler
import com.garcia.ignacio.storeclassic.ui.exceptions.ErrorReporter
import com.garcia.ignacio.storeclassic.ui.exceptions.ReportableError
import com.garcia.ignacio.storeclassic.ui.livedata.Event
import com.garcia.ignacio.storeclassic.ui.model.AddToCart
import com.garcia.ignacio.storeclassic.ui.model.ListState
import com.garcia.ignacio.storeclassic.ui.productlist.AppEffect
import com.garcia.ignacio.storeclassic.ui.productlist.ProductsEffect
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val ERROR_REPORT_ITEM_SEPARATOR = "\n====================\n"
private const val ERROR_REPORT_HEADER = "ERROR REPORT\n\n"

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StoreViewModel @Inject constructor(
    private val productsRepository: ProductsRepository,
    private val discountsRepository: DiscountsRepository,
    private val discountedProductsRepository: DiscountedProductsRepository,
    private val errorHandler: ErrorHandler,
    private val helper: StoreViewModelHelper,
    private val connectivityMonitor: ConnectivityMonitor,
) : ViewModel(), ErrorReporter {
    private val productsState = MutableLiveData<ListState<DiscountedProduct>>(ListState.Loading)
    fun getProductsState(): LiveData<ListState<DiscountedProduct>> = productsState
    private val discountsState = MutableLiveData<ListState<DiscountedProduct>>(ListState.Loading)
    fun getDiscountsState(): LiveData<ListState<DiscountedProduct>> = discountsState
    private val checkoutState = MutableLiveData<ListState<CheckoutRow>>(ListState.Loading)
    fun getCheckoutState(): LiveData<ListState<CheckoutRow>> = checkoutState
    var pendingAddToCart: AddToCart? = null
        private set
    private val productsEffect = MutableLiveData<Event<ProductsEffect>>(Event(ProductsEffect.Idle))
    private val appEffect = MutableLiveData<Event<AppEffect>>(Event(AppEffect.Idle))
    fun getAppEffect(): LiveData<Event<AppEffect>> = appEffect
    fun getProductsEffect(): LiveData<Event<ProductsEffect>> = productsEffect
    private val cart = MutableStateFlow<List<Product>>(emptyList())
    private var isConnectionAvailable = true
    private val discountsForProductCode = MutableStateFlow<String?>(null)

    fun getDiscountsForProduct(productCode: String? = null) {
        discountsForProductCode.value = productCode
    }

    private fun initializeDiscountsForProduct() {
        discountsForProductCode.flatMapLatest { productCode ->
            discountedProductsRepository.findDiscountedProducts(
                productCode?.let { setOf(productCode) } ?: emptySet()
            ).map { result ->
                result.onFailure {
                    errorHandler.handleErrors(listOf(it))
                }.getOrDefault(emptyList()).also { discountsState.value = ListState.Ready(it) }
            }
        }.launchIn(viewModelScope)
    }

    private fun initializeCheckoutData() {
        cart.flatMapLatest { cart ->
            discountedProductsRepository.findDiscountedProducts(
                cart.map { it.code }.toSet()
            ).map { result ->
                result.map {
                    helper.computeCheckoutData(cart, it)
                }.onFailure {
                    errorHandler.handleErrors(listOf(it))
                }.getOrDefault(emptyList()).also { checkoutState.value = ListState.Ready(it) }
            }
        }.launchIn(viewModelScope)
    }

    private fun initializeAllProductsWithDiscountsIfAny() {
        discountedProductsRepository.getAllProductsWithDiscountsIfAny().map { result ->
            result.onFailure {
                errorHandler.handleErrors(listOf(it))
            }.getOrDefault(emptyList())
                .also { productsState.value = ListState.Ready(it) }
        }.launchIn(viewModelScope)
    }

    fun clearCart() {
        cart.value = emptyList()
    }

    init {
        errorHandler.errorReporter = this
        initializeAllProductsWithDiscountsIfAny()
        initializeCheckoutData()
        initializeDiscountsForProduct()
        startMonitoringNetworkConnection()
        updateDataFromNetwork()
    }

    private fun updateDataFromNetwork() {
        viewModelScope.launch {
            productsState.value = ListState.Loading
            discountsState.value = ListState.Loading
            checkoutState.value = ListState.Loading
            updateDiscountsFromNetwork()
            updateProductsFromNetwork()
        }
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

    private suspend fun updateProductsFromNetwork() {
        productsRepository.updateProducts().onFailure {
            errorHandler.handleErrors(listOf(it), ErrorType.PRODUCT)
        }
    }

    private suspend fun updateDiscountsFromNetwork() {
        discountsRepository.updateDiscounts().onFailure {
            errorHandler.handleErrors(listOf(it), ErrorType.DISCOUNT)
        }
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
            cart.value = cart.value + toAdd
            productsEffect.value = Event(ProductsEffect.AddToCartConfirmed(addToCart))
            pendingAddToCart = null
        }
    }

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

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}