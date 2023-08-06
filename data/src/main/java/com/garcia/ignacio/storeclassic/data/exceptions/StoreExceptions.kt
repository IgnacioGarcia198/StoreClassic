package com.garcia.ignacio.storeclassic.data.exceptions

import com.garcia.ignacio.storeclassic.domain.models.Discount

sealed interface StoreException {
    class StageException(
        val stage: Stage,
        cause: Throwable?,
        message: String? = cause?.message
    ) : RuntimeException(message, cause), StoreException

    class UnimplementedDiscount(val discount: Discount) : Throwable(), StoreException

    class Misusing(message: String?) : RuntimeException(message), StoreException
}