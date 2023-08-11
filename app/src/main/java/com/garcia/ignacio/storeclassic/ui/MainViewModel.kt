package com.garcia.ignacio.storeclassic.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.garcia.ignacio.storeclassic.data.exceptions.ErrorHandler
import com.garcia.ignacio.storeclassic.data.exceptions.ReportableError
import com.garcia.ignacio.storeclassic.data.remote.ConnectivityMonitor
import com.garcia.ignacio.storeclassic.ui.livedata.Event
import com.garcia.ignacio.storeclassic.ui.productlist.AppEffect
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

private const val ERROR_REPORT_ITEM_SEPARATOR = "\n====================\n"
private const val ERROR_REPORT_HEADER = "ERROR REPORT\n\n"

@HiltViewModel
class MainViewModel @Inject constructor(
    private val connectivityMonitor: ConnectivityMonitor,
    private val errorHandler: ErrorHandler,
) : ViewModel() {
    private var wasConnected = true

    private val appEffect = MutableLiveData<Event<AppEffect>>(Event(AppEffect.Idle))
    fun getAppEffect(): LiveData<Event<AppEffect>> = appEffect

    init {
        startMonitoringErrors()
        startMonitoringNetworkConnection()
    }

    private fun startMonitoringErrors() {
        errorHandler.getErrors()
            .onEach { errors ->
                if (errors.isNotEmpty()) {
                    reportErrors(errors)
                }
            }.launchIn(viewModelScope)
    }

    private fun startMonitoringNetworkConnection() {
        connectivityMonitor.isNetworkConnectedFlow
            .onEach { isConnected ->
                when {
                    !wasConnected && isConnected -> {
                        appEffect.value = Event(AppEffect.ConnectionRestored)
                    }

                    wasConnected && !isConnected -> {
                        appEffect.value = Event(AppEffect.ConnectionLost)
                    }
                }
                wasConnected = isConnected
            }.launchIn(viewModelScope)
    }

    private fun reportErrors(errors: Set<ReportableError>) {
        val message = errors.joinToString("\n") { "- ${it.errorMessage}" }
        val report = errors.joinToString(
            ERROR_REPORT_ITEM_SEPARATOR,
            prefix = ERROR_REPORT_HEADER // TODO: Here we can add info on the device and OS
        ) { it.reportMessage }
        val reportableError = ReportableError(message, report)
        appEffect.value = Event(AppEffect.ReportErrors(reportableError))
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}