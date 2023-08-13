package com.garcia.ignacio.storeclassic.data.repository

import com.garcia.ignacio.storeclassic.common.extensions.mapError
import com.garcia.ignacio.storeclassic.data.exceptions.ErrorHandler
import com.garcia.ignacio.storeclassic.data.exceptions.ErrorType
import com.garcia.ignacio.storeclassic.data.exceptions.Stage
import com.garcia.ignacio.storeclassic.data.exceptions.StoreException
import com.garcia.ignacio.storeclassic.data.local.ProductsLocalDataStore
import com.garcia.ignacio.storeclassic.data.remote.ConnectivityMonitor
import com.garcia.ignacio.storeclassic.data.remote.StoreClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ProductsRepository @Inject constructor(
    private val storeClient: StoreClient,
    private val localDataStore: ProductsLocalDataStore,
    private val connectivityMonitor: ConnectivityMonitor,
    private val errorHandler: ErrorHandler,
) {
    suspend fun updateProducts() {
        withContext(Dispatchers.IO) {
            storeClient.getProducts()
                .mapError {
                    if (connectivityMonitor.isNetworkConnected) stageException(Stage.CLIENT, it)
                    else StoreException.DeviceOffline(it)
                }.mapCatching {
                    if (it.isNotEmpty()) localDataStore.updateProducts(it)
                }.onFailure { throwable ->
                    when (throwable) {
                        is StoreException -> throwable
                        else -> stageException(Stage.DB_WRITE, throwable)
                    }.also {
                        errorHandler.handleErrors(listOf(it), ErrorType.PRODUCT)
                    }
                }
        }
    }

    private fun stageException(
        stage: Stage,
        cause: Throwable?,
    ): StoreException.StageException = StoreException.StageException(
        stage = stage,
        errorType = ErrorType.PRODUCT,
        cause = cause
    )
}