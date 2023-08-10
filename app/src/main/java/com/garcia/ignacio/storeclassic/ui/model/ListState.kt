package com.garcia.ignacio.storeclassic.ui.model

sealed interface ListState<out T> {
    object Loading: ListState<Nothing>
    data class Ready<T>(val list: List<T>): ListState<T>
}