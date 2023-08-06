package com.garcia.ignacio.storeclassic.data.exceptions

import com.garcia.ignacio.storeclassic.domain.models.Discount

class StoreException(
    val stage: Stage,
    cause: Throwable?,
    message: String? = cause?.message
) : RuntimeException(message, cause)

class UnimplementedDiscount(val discount: Discount) : Throwable()