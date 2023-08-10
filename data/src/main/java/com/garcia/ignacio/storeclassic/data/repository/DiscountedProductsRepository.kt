package com.garcia.ignacio.storeclassic.data.repository

import com.garcia.ignacio.storeclassic.common.ResultList
import com.garcia.ignacio.storeclassic.data.exceptions.ErrorType
import com.garcia.ignacio.storeclassic.data.exceptions.Stage
import com.garcia.ignacio.storeclassic.data.exceptions.StoreException
import com.garcia.ignacio.storeclassic.data.local.DiscountedProductsLocalDataStore
import com.garcia.ignacio.storeclassic.domain.models.DiscountedProduct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class DiscountedProductsRepository @Inject constructor(
    private val localDataStore: DiscountedProductsLocalDataStore,
) {
    private val errors = mutableListOf<Throwable>()

    private fun stageException(
        stage: Stage,
        cause: Throwable?,
    ): StoreException.StageException = StoreException.StageException(
        stage = stage,
        errorType = ErrorType.DISCOUNT,
        cause = cause
    )

    private fun discountedProducts(
        productCodes: Set<String>
    ): Flow<List<DiscountedProduct>> =
        localDataStore.findDiscountedProducts(
            productCodes
        ).onEach {
            errors.clear()
        }.catch {
            errors.clear()
            errors.add(stageException(Stage.DB_WRITE, it))
            emit(emptyList())
        }

    fun findDiscountedProducts(
        productCodes: Set<String>
    ): Flow<ResultList<List<DiscountedProduct>>> = discountedProducts(
        productCodes
    ).map { discountedProducts ->
        ResultList(discountedProducts, errors)
    }.flowOn(Dispatchers.IO)
}