package com.garcia.ignacio.storeclassic.data.repository

import com.garcia.ignacio.storeclassic.data.exceptions.ErrorHandler
import com.garcia.ignacio.storeclassic.data.exceptions.StoreException
import com.garcia.ignacio.storeclassic.data.local.DiscountedProductsLocalDataStore
import com.garcia.ignacio.storeclassic.domain.models.DiscountedProduct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DiscountedProductsRepository @Inject constructor(
    private val localDataStore: DiscountedProductsLocalDataStore,
    private val errorHandler: ErrorHandler,
) {

    fun findDiscountedProducts(
        productCodes: Set<String>
    ): Flow<List<DiscountedProduct>> =
        localDataStore.findDiscountedProducts(
            productCodes
        ).catch {
            errorHandler.handleErrors(listOf(StoreException.ErrorRetrievingDiscountedProducts(it)))
        }.flowOn(Dispatchers.IO)

    fun getAllProductsWithDiscountsIfAny(): Flow<List<DiscountedProduct>> =
        localDataStore.getAllProductsAndDiscountIfAny().catch {
            errorHandler.handleErrors(listOf(StoreException.ErrorRetrievingDiscountedProducts(it)))
        }.flowOn(Dispatchers.IO)
}