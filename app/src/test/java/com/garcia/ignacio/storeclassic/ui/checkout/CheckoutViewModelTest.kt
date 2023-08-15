package com.garcia.ignacio.storeclassic.ui.checkout

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.garcia.ignacio.storeclassic.data.repository.DiscountedProductsRepository
import com.garcia.ignacio.storeclassic.domain.models.Discount
import com.garcia.ignacio.storeclassic.domain.models.DiscountedProduct
import com.garcia.ignacio.storeclassic.domain.models.Product
import com.garcia.ignacio.storeclassic.ui.model.ListState
import io.mockk.called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private const val PRODUCT_CODE = "code"
private const val PRODUCT_NAME = "name"
private const val PRODUCT_PRICE = 10.0

@RunWith(AndroidJUnit4::class)
class CheckoutViewModelTest {
    private val testProduct = Product(PRODUCT_CODE, PRODUCT_NAME, PRODUCT_PRICE)
    private val testDiscount = Discount.XForY(PRODUCT_CODE, 2, 3)
    private val discountedProducts = listOf(DiscountedProduct(testProduct, testDiscount))
    private val discountedProductsChannel = Channel<List<DiscountedProduct>>()
    private val discountedProductsFlow = discountedProductsChannel.receiveAsFlow()
    private val repository: DiscountedProductsRepository = mockk {
        coEvery { findDiscountedProducts(any()) }.returns(discountedProductsFlow)
    }
    private val computer: CheckoutDataComputer = mockk()
    private val cart = mutableListOf(testProduct)
    private val viewModel = CheckoutViewModel(repository, computer, cart)
    private val states = mutableListOf<ListState<CheckoutRow>>()
    private val testCheckoutRow = DiscountedCheckoutRow(
        listOf(testProduct),
        testDiscount,
        0.0,
        0.0
    )

    @Before
    fun setUp() {
        coEvery { computer.computeCheckoutData(any(), any()) }
            .returns(listOf(testCheckoutRow))
        states.clear()
        observeViewModel()
    }

    @Test
    fun `initially state is loading`() {
        verifyStates(ListState.Loading)
    }

    @Test
    fun `on startup ViewModel finds discounts for products in the cart`() {
        coVerify { repository.findDiscountedProducts(setOf(PRODUCT_CODE)) }
    }

    @Test
    fun `when repository delivers products, CheckoutDataComputer is used to compute checkout`() =
        runBlocking {
            discountedProductsChannel.send(discountedProducts)


            coVerify { computer.computeCheckoutData(cart, discountedProducts) }
        }

    @Test
    fun `when repository delivers products, state changes to Ready`() = runBlocking {
        discountedProductsChannel.send(discountedProducts)


        verifyStates(ListState.Loading, ListState.Ready(listOf(testCheckoutRow)))
    }

    @Test
    fun `clearCart() clears the shopping cart, skips calculation and delivers checkout data`() =
        runBlocking {
            coEvery { repository.findDiscountedProducts(any()) }.returns(flowOf(emptyList()))
            viewModel.clearCart()


            assertEquals(emptyList<Product>(), cart)
            coVerify { computer.wasNot(called) }
            verifyStates(ListState.Loading, ListState.Ready(emptyList()))
        }

    private fun observeViewModel() {
        viewModel.getCheckoutState().observeForever {
            println("state: $it")
            states.add(it)
        }
    }

    private fun verifyStates(vararg testStates: ListState<CheckoutRow>) {
        assertEquals(testStates.toList(), states)
    }
}