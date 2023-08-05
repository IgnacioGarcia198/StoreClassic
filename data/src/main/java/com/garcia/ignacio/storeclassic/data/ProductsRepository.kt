package com.garcia.ignacio.storeclassic.data

import com.garcia.ignacio.storeclassic.common.ResultList
import com.garcia.ignacio.storeclassic.data.local.ProductsLocalDataStore
import com.garcia.ignacio.storeclassic.data.remote.StoreClient
import com.garcia.ignacio.storeclassic.domain.models.Product
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class ProductsRepository @Inject constructor(
    private val storeClient: StoreClient,
    private val localDataStore: ProductsLocalDataStore,
) {
    private val errorStateFlow = MutableStateFlow(mutableListOf<Throwable>())

    fun getClientProducts(): Flow<List<Product>> = storeClient.getProducts()

    private val productsFlow: Flow<List<Product>> =
        storeClient.getProducts().onEach {
            errorStateFlow.value.clear()
        }.catch {
            errorStateFlow.value = mutableListOf(it)
            emit(emptyList())
        }.onEach {
            if (it.isNotEmpty()) localDataStore.updateProducts(it)
        }.catch { throwable ->
            errorStateFlow.value.add(throwable)
            emit(emptyList())
        }.flatMapConcat { localDataStore.getAllProducts() }

    val products: Flow<ResultList<List<Product>>> =
        productsFlow.combine(errorStateFlow) { list, errors ->
            ResultList(list, errors)
        }
}