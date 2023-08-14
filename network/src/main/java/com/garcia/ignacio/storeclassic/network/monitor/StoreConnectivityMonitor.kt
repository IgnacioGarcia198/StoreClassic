package com.garcia.ignacio.storeclassic.network.monitor

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.garcia.ignacio.storeclassic.data.remote.ConnectivityMonitor
import com.garcia.ignacio.storeclassic.testing.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoreConnectivityMonitor @Inject constructor(
    private val connectivityManager: ConnectivityManager,
    dispatcherProvider: DispatcherProvider,
) : ConnectivityMonitor {

    private val networkCallback = getNetworkCallback()

    private val _currentNetwork = MutableStateFlow(provideDefaultCurrentNetwork())

    override val isNetworkConnectedFlow: StateFlow<Boolean> =
        _currentNetwork
            .map { it.isConnected }
            .stateIn(
                scope = CoroutineScope(dispatcherProvider.io()),
                started = SharingStarted.WhileSubscribed(),
                initialValue = _currentNetwork.value.isConnected
            )

    init {
        startMonitoringNetworkConnection()
    }

    override val isNetworkConnected: Boolean
        get() = connectivityManager
            .getNetworkCapabilities(connectivityManager.activeNetwork)
            .areValid()

    override fun startMonitoringNetworkConnection() {
        if (_currentNetwork.value.isListening) {
            return
        }

        // Reset state before start listening
        _currentNetwork.update {
            provideDefaultCurrentNetwork()
                .copy(isListening = true)
        }

        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    override fun stopMonitoringNetworkConnection() {
        if (!_currentNetwork.value.isListening) {
            return
        }

        _currentNetwork.update {
            it.copy(isListening = false)
        }

        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    private fun getNetworkCallback() = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _currentNetwork.update {
                it.copy(isAvailable = true)
            }
        }

        override fun onLost(network: Network) {
            _currentNetwork.update {
                it.copy(
                    isAvailable = false,
                    networkCapabilities = null
                )
            }
        }

        override fun onUnavailable() {
            _currentNetwork.update {
                it.copy(
                    isAvailable = false,
                    networkCapabilities = null
                )
            }
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            _currentNetwork.update {
                it.copy(networkCapabilities = networkCapabilities)
            }
        }

        override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
            _currentNetwork.update {
                it.copy(isBlocked = blocked)
            }
        }
    }

    private fun provideDefaultCurrentNetwork(): CurrentNetwork {
        return CurrentNetwork(
            isListening = false,
            networkCapabilities = null,
            isAvailable = false,
            isBlocked = false
        )
    }

    private val CurrentNetwork.isConnected: Boolean
        get() = isListening &&
                isAvailable &&
                !isBlocked &&
                networkCapabilities.areValid()

    private fun NetworkCapabilities?.areValid(): Boolean = when {
        this == null -> false
        hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) &&
                hasValidTransport() -> true

        else -> false
    }

    private fun NetworkCapabilities.hasValidTransport() =
        hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                hasTransport(NetworkCapabilities.TRANSPORT_VPN) ||
                hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
}

private data class CurrentNetwork(
    val isListening: Boolean,
    val networkCapabilities: NetworkCapabilities?,
    val isAvailable: Boolean,
    val isBlocked: Boolean,
)