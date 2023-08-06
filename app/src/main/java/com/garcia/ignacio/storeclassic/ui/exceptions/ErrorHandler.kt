package com.garcia.ignacio.storeclassic.ui.exceptions

import com.garcia.ignacio.storeclassic.data.exceptions.Stage
import com.garcia.ignacio.storeclassic.data.exceptions.StoreException
import javax.inject.Inject

class ErrorHandler @Inject constructor() {

    fun handleErrors(errors: List<Throwable>, errorType: ErrorType) {
        errors.forEach { error ->
            handleError(error, errorType)
        }
    }

    private fun handleError(
        error: Throwable,
        errorType: ErrorType
    ) {
        when (error) {
            is StoreException -> {
                handleStoreException(error, errorType)
            }

            else -> {}
        }
    }

    private fun handleStoreException(
        error: StoreException,
        errorType: ErrorType
    ) {
        when (error) {
            is StoreException.StageException ->
                handleStageException(error, errorType)

            is StoreException.Misusing -> {}
            is StoreException.UnimplementedDiscount ->
                handleUnimplementedDiscount(errorType, error)

        }
    }

    private fun handleUnimplementedDiscount(
        errorType: ErrorType,
        error: StoreException
    ) {
        when (errorType) {
            ErrorType.PRODUCT ->
                throw StoreException.Misusing("Error not allowed for Product: $error")

            ErrorType.DISCOUNT -> {}
        }
    }

    private fun handleStageException(
        error: StoreException.StageException,
        errorType: ErrorType
    ) {
        when (error.stage) {
            Stage.CLIENT -> {}
            Stage.DB_WRITE -> {}
            Stage.DB_READ -> {}
        }
    }
}