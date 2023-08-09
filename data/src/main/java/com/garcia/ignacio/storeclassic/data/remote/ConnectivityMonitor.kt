package com.garcia.ignacio.storeclassic.data.remote

import kotlinx.coroutines.flow.StateFlow

interface ConnectivityMonitor {
    val isNetworkConnectedFlow: StateFlow<Boolean>

    val isNetworkConnected: Boolean

    fun startMonitoringNetworkConnection()

    fun stopMonitoringNetworkConnection()
}