package com.garcia.ignacio.storeclassic.ui.checkout

import androidx.recyclerview.widget.RecyclerView
import com.garcia.ignacio.storeclassic.databinding.CheckoutItemBinding
import java.text.DecimalFormat

private const val PRICE_FORMAT = "0.#"

class CheckoutViewHolder(
    private val binding: CheckoutItemBinding,
) : RecyclerView.ViewHolder(binding.root) {
    private val priceFormatter = DecimalFormat(PRICE_FORMAT)

    fun bind(row: CheckoutRow) {
        binding.productName.text = row.products.first().name
        binding.productPrice.text = priceFormatter.format(row.products.first().price)
        binding.productQuantity.text = row.products.size.toString()
        binding.amount.text = priceFormatter.format(row.amount)
        binding.discount.text =
            if (row is DiscountedCheckoutRow) priceFormatter.format(row.discountedPercent)
            else "0"
    }
}