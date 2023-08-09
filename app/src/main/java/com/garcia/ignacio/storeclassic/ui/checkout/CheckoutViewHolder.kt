package com.garcia.ignacio.storeclassic.ui.checkout

import androidx.recyclerview.widget.RecyclerView
import com.garcia.ignacio.storeclassic.databinding.CheckoutItemBinding
import com.garcia.ignacio.storeclassic.ui.formatting.StoreFormatter

class CheckoutViewHolder(
    private val binding: CheckoutItemBinding,
    private val formatter: StoreFormatter,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(row: CheckoutRow) {
        binding.productName.text = row.products.first().name
        binding.productPrice.text = formatter.formatPrice(row.products.first().price)
        binding.productQuantity.text = row.products.size.toString()
        binding.amount.text = formatter.formatPrice(row.amount)
        binding.discount.text = formatter.formatPercent(row.discountedPercent)
    }
}