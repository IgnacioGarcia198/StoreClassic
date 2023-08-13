package com.garcia.ignacio.storeclassic.data.repository

import com.garcia.ignacio.storeclassic.common.extensions.mapError
import com.garcia.ignacio.storeclassic.data.exceptions.ErrorHandler
import com.garcia.ignacio.storeclassic.data.exceptions.ErrorType
import com.garcia.ignacio.storeclassic.data.exceptions.Stage
import com.garcia.ignacio.storeclassic.data.exceptions.StoreException
import com.garcia.ignacio.storeclassic.data.local.DiscountsLocalDataStore
import com.garcia.ignacio.storeclassic.data.remote.ConnectivityMonitor
import com.garcia.ignacio.storeclassic.data.remote.StoreClient
import com.garcia.ignacio.storeclassic.domain.models.Discount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DiscountsRepository @Inject constructor(
    private val storeClient: StoreClient,
    private val localDataStore: DiscountsLocalDataStore,
    private val connectivityMonitor: ConnectivityMonitor,
    private val errorHandler: ErrorHandler,
) {
    suspend fun updateDiscounts() {
        withContext(Dispatchers.IO) {
            val errors = mutableListOf<Throwable>()
            storeClient.getDiscounts()
                .mapError {
                    if (connectivityMonitor.isNetworkConnected) stageException(Stage.CLIENT, it)
                    else StoreException.DeviceOffline(it)
                }.mapCatching { discounts ->
                    val (unimplemented, valid) = discounts.partition { it is Discount.Unimplemented }
                    unimplemented.forEach { errors.add(StoreException.UnimplementedDiscount(it)) }
                    if (valid.isNotEmpty()) localDataStore.updateDiscounts(valid)
                }.onFailure { throwable ->
                    when (throwable) {
                        is StoreException -> throwable
                        else -> stageException(Stage.DB_WRITE, throwable)
                    }.also {
                        errors.add(it)
                    }
                }.also {
                    errorHandler.handleErrors(errors, ErrorType.DISCOUNT)
                }
        }
    }

    private fun stageException(
        stage: Stage,
        cause: Throwable?,
    ): StoreException.StageException = StoreException.StageException(
        stage = stage,
        errorType = ErrorType.DISCOUNT,
        cause = cause
    )
}