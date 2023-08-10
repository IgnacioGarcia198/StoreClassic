package com.garcia.ignacio.storeclassic.data.repository

import com.garcia.ignacio.storeclassic.common.ResultList
import com.garcia.ignacio.storeclassic.data.exceptions.ErrorType
import com.garcia.ignacio.storeclassic.data.exceptions.Stage
import com.garcia.ignacio.storeclassic.data.exceptions.StoreException
import com.garcia.ignacio.storeclassic.data.exceptions.StoreException.UnimplementedDiscount
import com.garcia.ignacio.storeclassic.data.local.DiscountsLocalDataStore
import com.garcia.ignacio.storeclassic.data.remote.ConnectivityMonitor
import com.garcia.ignacio.storeclassic.data.remote.StoreClient
import com.garcia.ignacio.storeclassic.domain.models.Discount
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
class DiscountsRepository @Inject constructor(
    storeClient: StoreClient,
    private val localDataStore: DiscountsLocalDataStore,
    private val connectivityMonitor: ConnectivityMonitor,
) {
    private val errors = mutableListOf<Throwable>()

    private val discountsFlow: Flow<List<Discount>> =
        storeClient.getDiscounts().onEach {
            errors.clear()
        }.catch {
            val exception =
                if (connectivityMonitor.isNetworkConnected) stageException(Stage.CLIENT, it)
                else StoreException.DeviceOffline(it)
            errors.add(exception)
            emit(emptyList())
        }.onEach { discounts ->
            val (unimplemented, valid) = discounts.partition { it is Discount.Unimplemented }
            unimplemented.forEach { errors.add(UnimplementedDiscount(it)) }
            if (valid.isNotEmpty()) localDataStore.updateDiscounts(valid)
        }.catch {
            errors.add(stageException(Stage.DB_WRITE, it))
            emit(emptyList())
        }.flatMapConcat {
            localDataStore.getAllDiscounts()
        }.catch {
            errors.add(stageException(Stage.DB_READ, it))
            emit(emptyList())
        }

    private fun stageException(
        stage: Stage,
        cause: Throwable?,
    ): StoreException.StageException = StoreException.StageException(
        stage = stage,
        errorType = ErrorType.DISCOUNT,
        cause = cause
    )

    val discounts: Flow<ResultList<List<Discount>>> =
        discountsFlow.map { list ->
            ResultList(list, errors)
        }.flowOn(Dispatchers.IO)
}