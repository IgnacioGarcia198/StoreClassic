package com.garcia.ignacio.storeclassic.network.monitor

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.garcia.ignacio.storeclassic.androidtesting.CoroutineTestRule
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class StoreConnectivityMonitorTest {
    @get: Rule
    val coroutineTestRule =
        CoroutineTestRule()
    private val networkCallbackSlot = slot<ConnectivityManager.NetworkCallback>()
    private val connectivityManager: ConnectivityManager = mockk(relaxed = true) {
        captureCallback()
    }
    private val monitor = StoreConnectivityMonitor(connectivityManager)

    private val connectedStates = mutableListOf<Boolean>()


    @Before
    fun setUp() = runBlocking {
        connectedStates.clear()
        observeNetworkConnectedFlow()
    }

    @Test
    fun `on startup flow gets only false (initial default value)`() = runTest {
        coVerify { connectivityManager.registerDefaultNetworkCallback(any()) }
        assertEquals(listOf(false), connectedStates)
    }

    @Test
    fun `when all conditions are met flow value becomes true`() = runTest {
        networkCallbackSlot.captured.onAvailable(mockk())
        networkCallbackSlot.captured.onBlockedStatusChanged(mockk(), false)
        networkCallbackSlot.captured.onCapabilitiesChanged(mockk(), goodNetworkCapabilities)


        coVerify { connectivityManager.registerDefaultNetworkCallback(any()) }
        assertEquals(listOf(false, true), connectedStates)
    }

    @Test
    fun `when connection is lost flow value becomes false`() = runTest {
        networkCallbackSlot.captured.onAvailable(mockk())
        networkCallbackSlot.captured.onBlockedStatusChanged(mockk(), false)
        networkCallbackSlot.captured.onCapabilitiesChanged(mockk(), goodNetworkCapabilities)
        networkCallbackSlot.captured.onLost(mockk())


        coVerify { connectivityManager.registerDefaultNetworkCallback(any()) }
        assertEquals(listOf(false, true, false), connectedStates)
    }

    @Test
    fun `when connection is not available flow value becomes false`() = runTest {
        networkCallbackSlot.captured.onAvailable(mockk())
        networkCallbackSlot.captured.onBlockedStatusChanged(mockk(), false)
        networkCallbackSlot.captured.onCapabilitiesChanged(mockk(), goodNetworkCapabilities)
        networkCallbackSlot.captured.onUnavailable()


        coVerify { connectivityManager.registerDefaultNetworkCallback(any()) }
        assertEquals(listOf(false, true, false), connectedStates)
    }

    @Test
    fun `when connection is blocked flow value becomes false`() = runTest {
        networkCallbackSlot.captured.onAvailable(mockk())
        networkCallbackSlot.captured.onBlockedStatusChanged(mockk(), false)
        networkCallbackSlot.captured.onCapabilitiesChanged(mockk(), goodNetworkCapabilities)
        networkCallbackSlot.captured.onBlockedStatusChanged(mockk(), true)


        coVerify { connectivityManager.registerDefaultNetworkCallback(any()) }
        assertEquals(listOf(false, true, false), connectedStates)
    }

    @Test
    fun `when connection has not all required capabilities value becomes false`() = runTest {
        networkCallbackSlot.captured.onAvailable(mockk())
        networkCallbackSlot.captured.onBlockedStatusChanged(mockk(), false)
        networkCallbackSlot.captured.onCapabilitiesChanged(mockk(), goodNetworkCapabilities)
        networkCallbackSlot.captured.onCapabilitiesChanged(
            mockk(),
            getNetworkCapabilities(hasValidatedCapability = false)
        )


        coVerify { connectivityManager.registerDefaultNetworkCallback(any()) }
        assertEquals(listOf(false, true, false), connectedStates)
    }

    @Test
    fun `when connection has not a valid transport value becomes false`() = runTest {
        networkCallbackSlot.captured.onAvailable(mockk())
        networkCallbackSlot.captured.onBlockedStatusChanged(mockk(), false)
        networkCallbackSlot.captured.onCapabilitiesChanged(mockk(), goodNetworkCapabilities)
        networkCallbackSlot.captured.onCapabilitiesChanged(
            mockk(),
            getNetworkCapabilities(hasValidTransport = false)
        )


        coVerify { connectivityManager.registerDefaultNetworkCallback(any()) }
        assertEquals(listOf(false, true, false), connectedStates)
    }

    @Test
    fun `isNetworkConnected returns true when all conditions are met`() {
        every { connectivityManager.getNetworkCapabilities(any()) }
            .returns(goodNetworkCapabilities)


        assertTrue(monitor.isNetworkConnected)
    }

    @Test
    fun `isNetworkConnected returns false when connection has not all required capabilities`() {
        every { connectivityManager.getNetworkCapabilities(any()) }
            .returns(getNetworkCapabilities(hasValidatedCapability = false))


        assertFalse(monitor.isNetworkConnected)
    }

    @Test
    fun `isNetworkConnected returns false when connection has not a valid transport`() {
        every { connectivityManager.getNetworkCapabilities(any()) }
            .returns(getNetworkCapabilities(hasValidTransport = false))


        assertFalse(monitor.isNetworkConnected)
    }

    private fun observeNetworkConnectedFlow() {
        monitor.isNetworkConnectedFlow.onEach {
            connectedStates.add(it)
        }.launchIn(CoroutineScope(coroutineTestRule.testDispatcher))
    }

    private fun getNetworkCapabilities(
        hasInternetCapability: Boolean = true,
        hasValidatedCapability: Boolean = true,
        hasValidTransport: Boolean = true,
    ): NetworkCapabilities = mockk(relaxed = true) {
        every { hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) }
            .returns(hasInternetCapability)
        every { hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) }
            .returns(hasValidatedCapability)
        every { hasTransport(NetworkCapabilities.TRANSPORT_WIFI) }
            .returns(hasValidTransport)
    }

    private val goodNetworkCapabilities: NetworkCapabilities = getNetworkCapabilities()

    private fun ConnectivityManager.captureCallback() {
        every {
            registerDefaultNetworkCallback(capture(networkCallbackSlot))
        }.answers {}
    }
}