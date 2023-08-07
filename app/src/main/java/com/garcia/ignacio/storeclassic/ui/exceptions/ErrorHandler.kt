package com.garcia.ignacio.storeclassic.ui.exceptions

import com.garcia.ignacio.storeclassic.data.exceptions.ErrorType
import com.garcia.ignacio.storeclassic.data.exceptions.Stage
import com.garcia.ignacio.storeclassic.data.exceptions.StoreException
import javax.inject.Inject

class ErrorHandler @Inject constructor() {

    fun handleErrors(
        errors: List<Throwable>,
        errorType: ErrorType,
    ) {
        when (errorType) {
            ErrorType.PRODUCT ->
                handleAllErrors(errors)

            ErrorType.DISCOUNT -> {
                val (unimplemented, otherErrors) = errors.partition {
                    it is StoreException.UnimplementedDiscount
                }
                handleUnimplementedDiscounts(unimplemented)
                handleAllErrors(otherErrors)
            }
        }
    }

    private fun handleAllErrors(errors: List<Throwable>) {
        errors.forEach { error ->
            handleError(error)
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
    ) {
        when (error) {
            is StoreException -> {
                handleStoreException(error)
            }

            else -> {
                // report generic error
            }
        }
    }

    private fun handleStoreException(
        error: StoreException,
    ) {
        when (error) {
            is StoreException.StageException ->
                handleStageException(error)

            is StoreException.Misusing -> {
                // report to the developers
            }

            is StoreException.UnimplementedDiscount -> {
                // already handled
            }
        }
    }

    private fun handleStageException(
        error: StoreException.StageException,
    ) {
        when (error.errorType) {
            ErrorType.PRODUCT ->
                handleProductStageException(error)

            ErrorType.DISCOUNT ->
                handleDiscountStageException(error)
        }
    }

    private fun handleProductStageException(error: StoreException.StageException) {
        when (error.stage) {
            Stage.CLIENT -> {}
            Stage.DB_WRITE -> {}
            Stage.DB_READ -> {}
        }
    }

    private fun handleDiscountStageException(error: StoreException.StageException) {
        when (error.stage) {
            Stage.CLIENT -> {}
            Stage.DB_WRITE -> {}
            Stage.DB_READ -> {}
        }
    }
}