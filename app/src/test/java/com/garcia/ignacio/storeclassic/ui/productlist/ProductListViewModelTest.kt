package com.garcia.ignacio.storeclassic.ui.productlist

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.garcia.ignacio.storeclassic.data.repository.DiscountedProductsRepository
import com.garcia.ignacio.storeclassic.domain.models.Discount
import com.garcia.ignacio.storeclassic.domain.models.DiscountedProduct
import com.garcia.ignacio.storeclassic.domain.models.Product
import com.garcia.ignacio.storeclassic.ui.model.AddToCart
import com.garcia.ignacio.storeclassic.ui.model.ListState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private const val PRODUCT_CODE = "code"
private const val PRODUCT_NAME = "name"
private const val PRODUCT_PRICE = 10.0

@RunWith(AndroidJUnit4::class)
class ProductListViewModelTest {
    private val testProduct = Product(PRODUCT_CODE, PRODUCT_NAME, PRODUCT_PRICE)
    private val testDiscount = Discount.XForY(PRODUCT_CODE, 2, 3)
    private val discountedProducts = listOf(DiscountedProduct(testProduct, testDiscount))
    private val discountedProductsChannel = Channel<List<DiscountedProduct>>()
    private val discountedProductsFlow = discountedProductsChannel.consumeAsFlow()
    private val repository: DiscountedProductsRepository = mockk(relaxed = true) {
        coEvery { getAllProductsWithDiscountsIfAny() }.returns(discountedProductsFlow)
    }
    private val cart = mutableListOf<Product>()
    private val viewModel = ProductListViewModel(repository, cart)
    private val states = mutableListOf<ListState<DiscountedProduct>>()
    private val effects = mutableListOf<ProductsEffect>()

    @Before
    fun setUp() {
        states.clear()
        effects.clear()
        observeViewModel()
    }

    @Test
    fun `on startup ViewModel connects with Repository flow`() {
        coVerify { repository.getAllProductsWithDiscountsIfAny() }
    }

    @Test
    fun `initially state is loading and effect is idle`() {
        verifyStates(ListState.Loading)
        verifyEffects(ProductsEffect.Idle)
    }

    @Test
    fun `pendingAddToCart() triggers AddToCartConfirmation effect`() {
        viewModel.pendingAddToCart(testProduct, 1)


        verifyEffects(ProductsEffect.Idle, ProductsEffect.AddToCartConfirmation)
    }

    @Test
    fun `pendingAddToCartConfirmed() triggers AddToCartConfirmed effect`() {
        viewModel.pendingAddToCart(testProduct, 2)
        viewModel.pendingAddToCartConfirmed()


        verifyEffects(
            ProductsEffect.Idle,
            ProductsEffect.AddToCartConfirmation,
            ProductsEffect.AddToCartConfirmed(AddToCart(testProduct, 2))
        )
    }

    @Test
    fun `displayDiscounts() triggers DisplayDiscounts effect`() {
        viewModel.displayDiscounts(testProduct)


        verifyEffects(ProductsEffect.Idle, ProductsEffect.DisplayDiscounts(testProduct))
    }

    @Test
    fun `goToCheckout() triggers DisplayDiscounts effect`() {
        viewModel.goToCheckout()


        verifyEffects(ProductsEffect.Idle, ProductsEffect.Checkout)
    }

    @Test
    fun `when repository delivers products, state changes to Ready`() = runBlocking {
        discountedProductsChannel.send(discountedProducts)


        verifyStates(ListState.Loading, ListState.Ready(discountedProducts))
    }

    private fun observeViewModel() {
        viewModel.getProductsState().observeForever {
            println("state: $it")
            states.add(it)
        }
        viewModel.getProductsEffect().observeForever { event ->
            event.getContentIfNotHandled()?.let {
                println("effect: $it")
                effects.add(it)
            }
        }
    }

    private fun verifyEffects(vararg testEffects: ProductsEffect) {
        assertEquals(testEffects.toList(), effects)
    }

    private fun verifyStates(vararg testStates: ListState<DiscountedProduct>) {
        assertEquals(testStates.toList(), states)
    }
}