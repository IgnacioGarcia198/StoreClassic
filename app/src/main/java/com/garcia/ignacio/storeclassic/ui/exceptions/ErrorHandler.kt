package com.garcia.ignacio.storeclassic.ui.exceptions

import com.garcia.ignacio.storeclassic.data.exceptions.Stage
import com.garcia.ignacio.storeclassic.data.exceptions.StoreException
import javax.inject.Inject

class ErrorHandler @Inject constructor() {

    fun handleErrors(errors: List<Throwable>, errorType: ErrorType) {
        when (errorType) {
            ErrorType.PRODUCT ->
                errors.forEach { error ->
                    handleError(error, errorType)
                }

            ErrorType.DISCOUNT -> {
                val (unimplemented, otherErrors) = errors.partition {
                    it is StoreException.UnimplementedDiscount
                }
                handleUnimplementedDiscounts(unimplemented)
                otherErrors.forEach { error ->
                    handleError(error, errorType)
                }
            }
        }
    }

    private fun handleUnimplementedDiscounts(unimplemented: List<Throwable>) {
        unimplemented.map {
            (it as StoreException.UnimplementedDiscount).discount.productCode
        }.groupBy { it }.keys.let {
            // report unimplemented discount keys
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

            else -> {
                // report generic error
            }
        }
    }

    private fun handleStoreException(
        error: StoreException,
        errorType: ErrorType
    ) {
        when (error) {
            is StoreException.StageException ->
                handleStageException(error, errorType)

            is StoreException.Misusing -> {
                // report to the developers
            }

            else -> {
                // NOP
            }
        }
    }

    private fun handleStageException(
        error: StoreException.StageException,
        errorType: ErrorType
    ) {
        when (error.stage) {
            Stage.CLIENT -> handleClientError(error, errorType)
            Stage.DB_WRITE -> handleDbWriteError(error, errorType)
            Stage.DB_READ -> handleDbReadError(error, errorType)
        }
    }

    private fun handleClientError(error: StoreException.StageException, type: ErrorType) {
        when (type) {
            ErrorType.PRODUCT -> {}
            ErrorType.DISCOUNT -> {}
        }
    }

    private fun handleDbWriteError(error: StoreException.StageException, type: ErrorType) {
        when (type) {
            ErrorType.PRODUCT -> {}
            ErrorType.DISCOUNT -> {}
        }
    }

    private fun handleDbReadError(error: StoreException.StageException, type: ErrorType) {
        when (type) {
            ErrorType.PRODUCT -> {}
            ErrorType.DISCOUNT -> {}
        }
    }
}