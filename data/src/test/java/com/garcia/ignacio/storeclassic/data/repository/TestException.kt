package com.garcia.ignacio.storeclassic.data.repository

internal data class TestException(override val message: String = "test") : Throwable(message)