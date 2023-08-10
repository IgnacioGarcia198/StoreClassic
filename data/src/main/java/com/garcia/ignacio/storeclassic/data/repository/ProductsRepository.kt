package com.garcia.ignacio.storeclassic.data.repository

import com.garcia.ignacio.storeclassic.common.ResultList
import com.garcia.ignacio.storeclassic.data.exceptions.ErrorType
import com.garcia.ignacio.storeclassic.data.exceptions.Stage
import com.garcia.ignacio.storeclassic.data.exceptions.StoreException
import com.garcia.ignacio.storeclassic.data.local.ProductsLocalDataStore
import com.garcia.ignacio.storeclassic.data.remote.ConnectivityMonitor
import com.garcia.ignacio.storeclassic.data.remote.StoreClient
import com.garcia.ignacio.storeclassic.domain.models.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class ProductsRepository @Inject constructor(
    storeClient: StoreClient,
    private val localDataStore: ProductsLocalDataStore,
    private val connectivityMonitor: ConnectivityMonitor,
) {
    private val errors = mutableListOf<Throwable>()

    private val productsFlow: Flow<List<Product>> =
        storeClient.getProducts().onEach {
            errors.clear()
        }.catch {
            val exception =
                if (connectivityMonitor.isNetworkConnected) stageException(Stage.CLIENT, it)
                else StoreException.DeviceOffline(it)
            errors.clear()
            errors.add(exception)
            emit(emptyList())
        }.onEach {
            if (it.isNotEmpty()) localDataStore.updateProducts(it)
        }.catch {
            errors.add(stageException(Stage.DB_WRITE, it))
            emit(emptyList())
        }.flatMapConcat {
            localDataStore.getAllProducts()
        }.catch {
            errors.add(stageException(Stage.DB_READ, it))
            emit(emptyList())
        }

    private fun stageException(
        stage: Stage,
        cause: Throwable?,
    ): StoreException.StageException = StoreException.StageException(
        stage = stage,
        errorType = ErrorType.PRODUCT,
        cause = cause
    )

    val products: Flow<ResultList<List<Product>>> =
        productsFlow.map { list ->
            ResultList(list, errors)
        }.flowOn(Dispatchers.IO)
}