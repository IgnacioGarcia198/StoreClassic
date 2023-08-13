package com.garcia.ignacio.storeclassic.data.repository

import com.garcia.ignacio.storeclassic.data.exceptions.ErrorHandler
import com.garcia.ignacio.storeclassic.data.exceptions.ErrorType
import com.garcia.ignacio.storeclassic.data.exceptions.Stage
import com.garcia.ignacio.storeclassic.data.exceptions.StoreException
import com.garcia.ignacio.storeclassic.data.local.DiscountsLocalDataStore
import com.garcia.ignacio.storeclassic.data.remote.ConnectivityMonitor
import com.garcia.ignacio.storeclassic.data.remote.StoreClient
import com.garcia.ignacio.storeclassic.domain.models.Discount
import com.garcia.ignacio.storeclassic.domain.models.ProductCode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Test

class DiscountsRepositoryTest {
    private val storeClient: StoreClient = mockk()
    private val localDataStore: DiscountsLocalDataStore = mockk(relaxed = true)
    private val connectivityMonitor: ConnectivityMonitor = mockk()
    private val errorHandler: ErrorHandler = mockk(relaxed = true)
    private val repository = DiscountsRepository(
        storeClient, localDataStore, connectivityMonitor, errorHandler
    )

    private val discountList = listOf(
        Discount.XForY(
            ProductCode.VOUCHER.name,
            2,
            1
        ),
        Discount.BuyInBulk(
            ProductCode.TSHIRT.name,
            3,
            5.0
        ),
    )

    @Test
    fun `updateDiscounts() uses StoreClient to download and DiscountsLocalDataStore to update discounts`() =
        runBlocking {
            coEvery { storeClient.getDiscounts() }.returns(Result.success(discountList))


            repository.updateDiscounts()


            coVerify { storeClient.getDiscounts() }
            coVerify { localDataStore.updateDiscounts(discountList) }
        }

    @Test
    fun `updateDiscounts() returns failure with StageException if connection is available`() =
        runBlocking {
            coEvery { storeClient.getDiscounts() }.returns(
                Result.failure(TestException())
            )
            every { connectivityMonitor.isNetworkConnected }.returns(true)


            repository.updateDiscounts()


            coVerify { storeClient.getDiscounts() }
            val errorSlot = slot<List<Throwable>>()
            coVerify { errorHandler.handleErrors(capture(errorSlot)) }
            val error = errorSlot.captured.first() as StoreException.StageException
            assertEquals("test", error.message)
            assertEquals(ErrorType.DISCOUNT, error.errorType)
            assertEquals(Stage.CLIENT, error.stage)
            assertEquals(TestException(), error.cause)
        }

    @Test
    fun `updateDiscounts() returns failure with DeviceOffline if connection is not available`() =
        runBlocking {
            coEvery { storeClient.getDiscounts() }.returns(
                Result.failure(TestException())
            )
            every { connectivityMonitor.isNetworkConnected }.returns(false)


            repository.updateDiscounts()


            coVerify { storeClient.getDiscounts() }
            val errorSlot = slot<List<Throwable>>()
            coVerify { errorHandler.handleErrors(capture(errorSlot)) }
            val error = errorSlot.captured.first() as StoreException.DeviceOffline
            assertEquals("test", error.message)
            assertEquals(TestException(), error.cause)
        }

    @Test
    fun `updateDiscounts() does not update db if client has an error`() = runBlocking {
        coEvery { storeClient.getDiscounts() }.returns(
            Result.failure(TestException())
        )
        every { connectivityMonitor.isNetworkConnected }.returns(true)


        repository.updateDiscounts()


        coVerify { storeClient.getDiscounts() }
        coVerify(exactly = 0) { localDataStore.updateDiscounts(any()) }
    }

    @Test
    fun `updateDiscounts() returns failure with StageException if updating db fails`() =
        runBlocking {
            coEvery { storeClient.getDiscounts() }.returns(Result.success(discountList))
            coEvery { localDataStore.updateDiscounts(any()) }.throws(TestException())


            repository.updateDiscounts()


            coVerify { storeClient.getDiscounts() }
            coVerify { localDataStore.updateDiscounts(discountList) }
            val errorSlot = slot<List<Throwable>>()
            coVerify { errorHandler.handleErrors(capture(errorSlot)) }
            val error = errorSlot.captured.first() as StoreException.StageException
            assertEquals("test", error.message)
            assertEquals(ErrorType.DISCOUNT, error.errorType)
            assertEquals(Stage.DB_WRITE, error.stage)
            assertEquals(TestException(), error.cause)
        }

    @Test
    fun `updateDiscounts() does not update db with empty discounts`() =
        runBlocking {
            coEvery { storeClient.getDiscounts() }.returns(Result.success(emptyList()))
            coEvery { localDataStore.updateDiscounts(any()) }.throws(TestException())


            repository.updateDiscounts()


            coVerify { storeClient.getDiscounts() }
            coVerify(exactly = 0) { localDataStore.updateDiscounts(discountList) }
        }

    private data class TestException(override val message: String = "test") : Throwable(message)
}