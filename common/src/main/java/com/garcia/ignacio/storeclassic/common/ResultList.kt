package com.garcia.ignacio.storeclassic.common

data class ResultList<T>(
    val result: T,
    val errors: List<Throwable> = emptyList()
)