package com.garcia.ignacio.storeclassic.ui.discountlist

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.garcia.ignacio.storeclassic.data.repository.DiscountedProductsRepository
import com.garcia.ignacio.storeclassic.domain.models.Discount
import com.garcia.ignacio.storeclassic.domain.models.DiscountedProduct
import com.garcia.ignacio.storeclassic.domain.models.Product
import com.garcia.ignacio.storeclassic.ui.model.ListState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase
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
class DiscountsViewModelTest {
    private val testProduct = Product(PRODUCT_CODE, PRODUCT_NAME, PRODUCT_PRICE)
    private val testDiscount = Discount.XForY(PRODUCT_CODE, 2, 3)
    private val discountedProducts = listOf(DiscountedProduct(testProduct, testDiscount))
    private val discountedProductsChannel = Channel<List<DiscountedProduct>>()
    private val discountedProductsFlow = discountedProductsChannel.consumeAsFlow()
    private val repository: DiscountedProductsRepository = mockk(relaxed = true) {
        coEvery { findDiscountedProducts(any()) }.returns(discountedProductsFlow)
    }
    private val viewModel = DiscountsViewModel(repository)
    private val states = mutableListOf<ListState<DiscountedProduct>>()

    @Before
    fun setUp() {
        states.clear()
        observeViewModel()
    }

    @Test
    fun `initially state is loading`() {
        verifyStates(ListState.Loading)
    }

    @Test
    fun `initialize() calls Repository_findDiscountedProducts with given product code`() {
        viewModel.initialize(PRODUCT_CODE)

        coVerify { repository.findDiscountedProducts(setOf(PRODUCT_CODE)) }
    }

    @Test
    fun `initialize() with null calls Repository_findDiscountedProducts with empty set`() {
        viewModel.initialize(null)

        coVerify { repository.findDiscountedProducts(emptySet()) }
    }

    @Test
    fun `when repository delivers products, state changes to Ready`() = runBlocking {
        viewModel.initialize(null)
        discountedProductsChannel.send(discountedProducts)


        verifyStates(ListState.Loading, ListState.Ready(discountedProducts))
    }

    private fun observeViewModel() {
        viewModel.getDiscountsState().observeForever {
            println("state: $it")
            states.add(it)
        }
    }

    private fun verifyStates(vararg testStates: ListState<DiscountedProduct>) {
        TestCase.assertEquals(testStates.toList(), states)
    }
}