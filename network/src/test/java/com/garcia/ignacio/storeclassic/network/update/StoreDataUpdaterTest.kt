package com.garcia.ignacio.storeclassic.network.update

import com.garcia.ignacio.storeclassic.androidtesting.CoroutineTestRule
import com.garcia.ignacio.storeclassic.data.remote.ConnectivityMonitor
import com.garcia.ignacio.storeclassic.data.repository.DiscountsRepository
import com.garcia.ignacio.storeclassic.data.repository.ProductsRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StoreDataUpdaterTest {
    @get: Rule
    val coroutineTestRule =
        CoroutineTestRule()

    private val discountsRepository: DiscountsRepository = mockk(relaxed = true)
    private val productsRepository: ProductsRepository = mockk(relaxed = true)
    private val connectivityMonitor: ConnectivityMonitor = mockk()
    private val isConnectedFlow = MutableStateFlow(true)
    private val updater = StoreDataUpdater(
        productsRepository,
        discountsRepository,
        connectivityMonitor,
        coroutineTestRule.testDispatcherProvider
    )

    @Before
    fun setUp() {
        every { connectivityMonitor.isNetworkConnectedFlow }.returns(isConnectedFlow)
    }

    @Test
    fun `updater starts observing connectivity monitor flow on initialization`() {
        updater.initialize()


        verify { connectivityMonitor.isNetworkConnectedFlow }
    }

    @Test
    fun `updater updates data from network on initialization`() = runTest {
        updater.initialize()
        advanceUntilIdle()


        coVerify { productsRepository.updateProducts() }
        coVerify { discountsRepository.updateDiscounts() }
    }

    @Test
    fun `updater updates data from network when connectivity is restored`() = runTest {
        updater.initialize()
        isConnectedFlow.value = false
        advanceUntilIdle()
        isConnectedFlow.value = true
        advanceUntilIdle()


        coVerify(exactly = 2) { productsRepository.updateProducts() }
        coVerify(exactly = 2) { discountsRepository.updateDiscounts() }
    }
}