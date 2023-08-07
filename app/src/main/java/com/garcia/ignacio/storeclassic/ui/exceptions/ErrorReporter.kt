package com.garcia.ignacio.storeclassic.ui.exceptions

interface ErrorReporter {
    fun reportErrors(errors: List<ReportableError>)
}