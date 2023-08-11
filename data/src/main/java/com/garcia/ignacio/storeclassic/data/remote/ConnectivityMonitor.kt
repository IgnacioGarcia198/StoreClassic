package com.garcia.ignacio.storeclassic.data.remote

import kotlinx.coroutines.flow.SharedFlow

interface ConnectivityMonitor {
    val isNetworkConnectedFlow: SharedFlow<Boolean>

    val isNetworkConnected: Boolean

    fun startMonitoringNetworkConnection()

    fun stopMonitoringNetworkConnection()
}