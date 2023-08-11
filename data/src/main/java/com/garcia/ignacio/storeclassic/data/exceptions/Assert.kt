package com.garcia.ignacio.storeclassic.data.exceptions

import com.garcia.ignacio.storeclassic.common.buildconfig.BuildConfig

fun assertOnDebug(message: String = "", assertion: () -> Boolean) {
    if (BuildConfig.DEBUG && !assertion()) {
        throw StoreException.Misusing(message)
    }
}