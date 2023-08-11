package com.garcia.ignacio.storeclassic.ui.productlist

import com.garcia.ignacio.storeclassic.data.exceptions.ReportableError

sealed interface AppEffect {
    object Idle : AppEffect
    data class ReportErrors(val compoundError: ReportableError) : AppEffect
    object ConnectionRestored : AppEffect
    object ConnectionLost : AppEffect
}