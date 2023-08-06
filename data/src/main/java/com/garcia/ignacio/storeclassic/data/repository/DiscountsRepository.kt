package com.garcia.ignacio.storeclassic.data.repository

import com.garcia.ignacio.storeclassic.common.ResultList
import com.garcia.ignacio.storeclassic.data.exceptions.Stage
import com.garcia.ignacio.storeclassic.data.exceptions.StoreException.StageException
import com.garcia.ignacio.storeclassic.data.exceptions.StoreException.UnimplementedDiscount
import com.garcia.ignacio.storeclassic.data.local.DiscountsLocalDataStore
import com.garcia.ignacio.storeclassic.data.remote.StoreClient
import com.garcia.ignacio.storeclassic.domain.models.Discount
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
class DiscountsRepository @Inject constructor(
    storeClient: StoreClient,
    private val localDataStore: DiscountsLocalDataStore,
) {
    private val errorStateFlow = MutableStateFlow(mutableListOf<Throwable>())

    private val discountsFlow: Flow<List<Discount>> =
        storeClient.getDiscounts().onEach {
            errorStateFlow.value.clear()
        }.catch {
            errorStateFlow.value = mutableListOf(StageException(Stage.CLIENT, it))
            emit(emptyList())
        }.onEach { discounts ->
            val (unimplemented, valid) = discounts.partition { it is Discount.Unimplemented }
            unimplemented.forEach { errorStateFlow.value.add(UnimplementedDiscount(it)) }
            if (valid.isNotEmpty()) localDataStore.updateDiscounts(valid)
        }.catch {
            errorStateFlow.value.add(StageException(Stage.DB_WRITE, it))
            emit(emptyList())
        }.flatMapConcat {
            localDataStore.getAllDiscounts()
        }.catch {
            errorStateFlow.value.add(StageException(Stage.DB_READ, it))
            emit(emptyList())
        }


    val discounts: Flow<ResultList<List<Discount>>> =
        discountsFlow.combine(errorStateFlow) { list, errors ->
            ResultList(list, errors)
        }.flowOn(Dispatchers.IO)
}