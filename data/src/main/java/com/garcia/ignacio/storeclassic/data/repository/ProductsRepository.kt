package com.garcia.ignacio.storeclassic.data.repository

import com.garcia.ignacio.storeclassic.common.ResultList
import com.garcia.ignacio.storeclassic.data.exceptions.Stage
import com.garcia.ignacio.storeclassic.data.exceptions.StoreException.StageException
import com.garcia.ignacio.storeclassic.data.local.ProductsLocalDataStore
import com.garcia.ignacio.storeclassic.data.remote.StoreClient
import com.garcia.ignacio.storeclassic.domain.models.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class ProductsRepository @Inject constructor(
    storeClient: StoreClient,
    private val localDataStore: ProductsLocalDataStore,
) {
    private val errorStateFlow = MutableStateFlow(mutableListOf<Throwable>())

    private val productsFlow: Flow<List<Product>> =
        storeClient.getProducts().onEach {
            errorStateFlow.value.clear()
        }.catch {
            errorStateFlow.value = mutableListOf(StageException(Stage.CLIENT, it))
            emit(emptyList())
        }.onEach {
            if (it.isNotEmpty()) localDataStore.updateProducts(it)
        }.catch {
            errorStateFlow.value.add(StageException(Stage.DB_WRITE, it))
            emit(emptyList())
        }.flatMapConcat {
            localDataStore.getAllProducts()
        }.catch {
            errorStateFlow.value.add(StageException(Stage.DB_READ, it))
            emit(emptyList())
        }


    val products: Flow<ResultList<List<Product>>> =
        productsFlow.combine(errorStateFlow) { list, errors ->
            ResultList(list, errors)
        }.flowOn(Dispatchers.IO)
}