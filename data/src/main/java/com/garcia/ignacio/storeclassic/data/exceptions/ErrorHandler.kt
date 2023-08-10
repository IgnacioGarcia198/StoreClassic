package com.garcia.ignacio.storeclassic.data.exceptions

import kotlinx.coroutines.flow.StateFlow

interface ErrorHandler {
    fun handleErrors(
        errors: List<Throwable>,
        errorType: ErrorType? = null,
    )

    fun getErrors(): StateFlow<Set<ReportableError>>
}