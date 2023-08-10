package com.garcia.ignacio.storeclassic.common.extensions

fun <Type> Result<Type>.mapError(block: (Throwable) -> Throwable): Result<Type> =
    fold(
        onSuccess = { this },
        onFailure = { Result.failure(block(it)) }
    )