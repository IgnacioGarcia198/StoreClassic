package com.garcia.ignacio.storeclassic.ui.formatting

import java.text.DecimalFormat
import javax.inject.Inject

private const val PRICE_FORMAT = "0.#€"
private const val PERCENT_FORMAT = "0.#'%'"

class StoreFormatter @Inject constructor() {
    private val priceFormatter = DecimalFormat(PRICE_FORMAT)
    private val percentFormatter = DecimalFormat(PERCENT_FORMAT)

    fun formatPrice(price: Double): String = priceFormatter.format(price)

    fun formatPercent(percentage: Double): String = percentFormatter.format(percentage)
}