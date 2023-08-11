package com.garcia.ignacio.storeclassic.common.buildconfig

object BuildConfig {
    var DEBUG = false
        private set

    fun setDebugOnStartup() {
        DEBUG = true
    }
}