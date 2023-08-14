package com.garcia.ignacio.storeclassic.data.repository

import com.garcia.ignacio.storeclassic.data.exceptions.ErrorHandler
import com.garcia.ignacio.storeclassic.data.exceptions.ErrorType
import com.garcia.ignacio.storeclassic.data.exceptions.Stage
import com.garcia.ignacio.storeclassic.data.exceptions.StoreException
import com.garcia.ignacio.storeclassic.data.local.ProductsLocalDataStore
import com.garcia.ignacio.storeclassic.data.remote.ConnectivityMonitor
import com.garcia.ignacio.storeclassic.data.remote.StoreClient
import com.garcia.ignacio.storeclassic.domain.models.Product
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import org.junit.Test

private const val PRODUCT_CODE = "code"
private const val PRODUCT_NAME = "name"
private const val PRODUCT_PRICE = 10.0

class ProductsRepositoryTest {
    private val storeClient: StoreClient = mockk()
    private val localDataStore: ProductsLocalDataStore = mockk(relaxed = true)
    private val connectivityMonitor: ConnectivityMonitor = mockk()
    private val errorHandler: ErrorHandler = mockk(relaxed = true)
    private val repository = ProductsRepository(
        storeClient, localDataStore, connectivityMonitor, errorHandler
    )
    private val productList = (1..3).map { Product(PRODUCT_CODE, PRODUCT_NAME, PRODUCT_PRICE) }

    @Test
    fun `updateProducts() uses StoreClient to download and ProductsLocalDataStore to update products`() =
        runBlocking {
            coEvery { storeClient.getProducts() }.returns(Result.success(productList))


            repository.updateProducts()


            coVerify { storeClient.getProducts() }
            coVerify { localDataStore.updateProducts(productList) }
        }

    @Test
    fun `updateProducts() adds error with StageException if connection is available`() =
        runBlocking {
            coEvery { storeClient.getProducts() }.returns(
                Result.failure(TestException())
            )
            every { connectivityMonitor.isNetworkConnected }.returns(true)


            repository.updateProducts()


            coVerify { storeClient.getProducts() }
            val errorSlot = slot<List<Throwable>>()
            coVerify { errorHandler.handleErrors(capture(errorSlot), ErrorType.PRODUCT) }
            val error = errorSlot.captured.first() as StoreException.StageException
            TestCase.assertEquals("test", error.message)
            TestCase.assertEquals(ErrorType.PRODUCT, error.errorType)
            TestCase.assertEquals(Stage.CLIENT, error.stage)
            TestCase.assertEquals(TestException(), error.cause)
        }

    @Test
    fun `updateProducts() adds error with DeviceOffline if connection is not available`() =
        runBlocking {
            coEvery { storeClient.getProducts() }.returns(
                Result.failure(TestException())
            )
            every { connectivityMonitor.isNetworkConnected }.returns(false)


            repository.updateProducts()


            coVerify { storeClient.getProducts() }
            val errorSlot = slot<List<Throwable>>()
            coVerify { errorHandler.handleErrors(capture(errorSlot), ErrorType.PRODUCT) }
            val error = errorSlot.captured.first() as StoreException.DeviceOffline
            TestCase.assertEquals("test", error.message)
            TestCase.assertEquals(TestException(), error.cause)
        }

    @Test
    fun `updateProducts() does not update db if client has an error`() = runBlocking {
        coEvery { storeClient.getProducts() }.returns(
            Result.failure(TestException())
        )
        every { connectivityMonitor.isNetworkConnected }.returns(true)


        repository.updateProducts()


        coVerify { storeClient.getProducts() }
        coVerify(exactly = 0) { localDataStore.updateProducts(any()) }
    }

    @Test
    fun `updateProducts() adds error with StageException if updating db fails`() =
        runBlocking {
            coEvery { storeClient.getProducts() }.returns(Result.success(productList))
            coEvery { localDataStore.updateProducts(any()) }.throws(TestException())


            repository.updateProducts()


            coVerify { storeClient.getProducts() }
            coVerify { localDataStore.updateProducts(productList) }
            val errorSlot = slot<List<Throwable>>()
            coVerify { errorHandler.handleErrors(capture(errorSlot), ErrorType.PRODUCT) }
            val error = errorSlot.captured.first() as StoreException.StageException
            TestCase.assertEquals("test", error.message)
            TestCase.assertEquals(ErrorType.PRODUCT, error.errorType)
            TestCase.assertEquals(Stage.DB_WRITE, error.stage)
            TestCase.assertEquals(TestException(), error.cause)
        }


    @Test
    fun `updateProducts() does not update db with empty products`() =
        runBlocking {
            coEvery { storeClient.getProducts() }.returns(Result.success(emptyList()))
            coEvery { localDataStore.updateProducts(any()) }.throws(TestException())


            repository.updateProducts()


            coVerify { storeClient.getProducts() }
            coVerify(exactly = 0) { localDataStore.updateProducts(any()) }
        }
}