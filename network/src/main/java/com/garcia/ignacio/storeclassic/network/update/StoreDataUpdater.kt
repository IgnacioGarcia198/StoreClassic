package com.garcia.ignacio.storeclassic.network.update

import com.garcia.ignacio.storeclassic.data.remote.ConnectivityMonitor
import com.garcia.ignacio.storeclassic.data.repository.DiscountsRepository
import com.garcia.ignacio.storeclassic.data.repository.ProductsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val UPDATE_FROM_NETWORK_AFTER_CONNECTION_RESTORED_DELAY = 100L

class StoreDataUpdater @Inject constructor(
    private val productsRepository: ProductsRepository,
    private val discountsRepository: DiscountsRepository,
    private val connectivityMonitor: ConnectivityMonitor,
) {
    private var wasConnected = true
    private val scope = CoroutineScope(Dispatchers.IO)

    fun initialize() {
        startMonitoringNetworkConnection()
        updateDataFromNetwork()
    }

    private fun startMonitoringNetworkConnection() {
        connectivityMonitor.isNetworkConnectedFlow
            .onEach { isConnected ->
                if (!wasConnected && isConnected) {
                    updateDataFromNetwork(UPDATE_FROM_NETWORK_AFTER_CONNECTION_RESTORED_DELAY)
                }
                wasConnected = isConnected
            }.launchIn(scope)
    }

    private fun updateDataFromNetwork(delay: Long = 0L) {
        scope.launch {
            delay(delay)
            updateDiscountsFromNetwork()
            updateProductsFromNetwork()
        }
    }

    private suspend fun updateProductsFromNetwork() {
        productsRepository.updateProducts()
    }

    private suspend fun updateDiscountsFromNetwork() {
        discountsRepository.updateDiscounts()
    }
}