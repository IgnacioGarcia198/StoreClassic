package com.garcia.ignacio.storeclassic.ui.livedata

/**
 * Used as a wrapper for data that is exposed via a LiveData/StateFlow that represents an event.
 */
@Suppress("unused")
data class Event<out T>(private val content: T) {

    var hasBeenHandled = false
        private set

    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    fun getContent(): T {
        hasBeenHandled = true
        return content
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = content
}