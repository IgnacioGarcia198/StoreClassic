package com.garcia.ignacio.storeclassic.ui.exceptions

import android.app.Application
import com.garcia.ignacio.storeclassic.BuildConfig
import com.garcia.ignacio.storeclassic.R
import com.garcia.ignacio.storeclassic.data.exceptions.ErrorType
import com.garcia.ignacio.storeclassic.data.exceptions.Stage
import com.garcia.ignacio.storeclassic.data.exceptions.StoreException
import javax.inject.Inject

class ErrorHandler @Inject constructor(
    private val context: Application
) {
    lateinit var errorReporter: ErrorReporter
    private val reportableErrors = mutableSetOf<ReportableError>()

    fun handleErrors(
        errors: List<Throwable>,
        errorType: ErrorType,
    ) {
        reportableErrors.clear()
        if (errors.isEmpty()) return
        when (errorType) {
            ErrorType.PRODUCT ->
                handleAllErrors(errors)

            ErrorType.DISCOUNT -> {
                val (unimplemented, otherErrors) = errors.partition {
                    it is StoreException.UnimplementedDiscount
                }
                if (unimplemented.isNotEmpty()) {
                    handleUnimplementedDiscounts(unimplemented)
                }
                if (otherErrors.isNotEmpty()) {
                    handleAllErrors(otherErrors)
                }
            }
        }
        errorReporter.reportErrors(reportableErrors)
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
            val unimplementedDiscountTypes = it.joinToString()
            val devMessage = "Unimplemented discounts were received: $unimplementedDiscountTypes"

            val errorMessage = if (BuildConfig.DEBUG) {
                devMessage
            } else {
                context.getString(R.string.unimplemented_discounts_user_feedback)
            }
            addReportableError(errorMessage, devMessage)
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
                val errorMessage =
                    if (BuildConfig.DEBUG) {
                        "An error occurred: ${error.message.orEmpty()}"
                    } else {
                        context.getString(R.string.generic_error_user_feedback)
                    }
                addReportableError(errorMessage, error.stackTraceToString())
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
                if (BuildConfig.DEBUG) {
                    throw error
                }
            }

            is StoreException.UnimplementedDiscount -> {
                if (BuildConfig.DEBUG) {
                    throw StoreException.Misusing("UnimplementedDiscount not available for Product")
                }
            }

            is StoreException.DeviceOffline ->
                handleDeviceOffline(error)
        }
    }

    private fun handleDeviceOffline(error: StoreException.DeviceOffline) {
        val errorMessage =
            if (BuildConfig.DEBUG) {
                "Device is offline: ${error.message.orEmpty()}"
            } else {
                context.getString(R.string.device_offline_user_feedback)
            }
        addReportableError(
            errorMessage,
            "Products client error: ${error.stackTraceToString()}"
        )
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
            Stage.CLIENT -> {
                val errorMessage =
                    if (BuildConfig.DEBUG) {
                        "Products client error: ${error.message.orEmpty()}"
                    } else {
                        context.getString(R.string.products_network_error_user_feedback)
                    }
                addReportableError(
                    errorMessage,
                    "Products client error: ${error.stackTraceToString()}"
                )
            }

            Stage.DB_WRITE -> {
                val errorMessage =
                    if (BuildConfig.DEBUG) {
                        "Products database write error: ${error.message.orEmpty()}"
                    } else {
                        context.getString(R.string.product_db_write_error_user_feedback)
                    }
                addReportableError(
                    errorMessage,
                    "Products database write error: ${error.stackTraceToString()}"
                )
            }

            Stage.DB_READ -> {
                val errorMessage =
                    if (BuildConfig.DEBUG) {
                        "Products database read error: ${error.message.orEmpty()}"
                    } else {
                        context.getString(R.string.product_db_read_error_user_feedback)
                    }
                addReportableError(
                    errorMessage,
                    "Products database read error: ${error.stackTraceToString()}"
                )
            }
        }
    }

    private fun handleDiscountStageException(error: StoreException.StageException) {
        when (error.stage) {
            Stage.CLIENT -> {
                val errorMessage =
                    if (BuildConfig.DEBUG) {
                        "Discounts client error: ${error.message.orEmpty()}"
                    } else {
                        context.getString(R.string.discounts_network_error_user_feedback)
                    }
                addReportableError(
                    errorMessage,
                    "Discounts client error: ${error.stackTraceToString()}"
                )
            }

            Stage.DB_WRITE -> {
                val errorMessage =
                    if (BuildConfig.DEBUG) {
                        "Discounts database write error: ${error.message.orEmpty()}"
                    } else {
                        context.getString(R.string.discount_db_write_error_user_feedback)
                    }
                addReportableError(
                    errorMessage,
                    "Discounts database write error: ${error.stackTraceToString()}"
                )
            }

            Stage.DB_READ -> {
                val errorMessage =
                    if (BuildConfig.DEBUG) {
                        "Discounts database read error: ${error.message.orEmpty()}"
                    } else {
                        context.getString(R.string.discount_db_read_error_user_feedback)
                    }
                addReportableError(
                    errorMessage,
                    "Discounts database read error: ${error.stackTraceToString()}"
                )
            }
        }
    }

    private fun addReportableError(
        errorMessage: String,
        reportMessage: String,
    ) {
        reportableErrors.add(ReportableError(errorMessage, reportMessage))
    }
}