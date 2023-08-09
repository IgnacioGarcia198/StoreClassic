package com.garcia.ignacio.storeclassic.ui.checkout

import android.graphics.Typeface
import androidx.recyclerview.widget.RecyclerView
import com.garcia.ignacio.storeclassic.R
import com.garcia.ignacio.storeclassic.databinding.CheckoutItemBinding
import com.garcia.ignacio.storeclassic.ui.formatting.StoreFormatter

private const val NO_VALUE = "----"

class CheckoutViewHolder(
    private val binding: CheckoutItemBinding,
    private val formatter: StoreFormatter,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(row: CheckoutRow) {
        when (row) {
            is TotalCheckoutRow -> bindTotalCheckoutRow(row)
            else -> bindCheckoutRow(row)
        }

    }

    private fun bindTotalCheckoutRow(row: TotalCheckoutRow) {
        binding.productName.text = itemView.context.getString(R.string.checkout_total)
        binding.productName.setTypeface(binding.productName.typeface, Typeface.BOLD)
        binding.productPrice.text = NO_VALUE
        binding.productQuantity.text = row.quantity.toString()
        binding.amount.text = formatter.formatPrice(row.amount)
        binding.amount.setTypeface(binding.amount.typeface, Typeface.BOLD)
        binding.discount.text = formatter.formatPercent(row.discountedPercent)
        binding.discount.setTypeface(binding.discount.typeface, Typeface.BOLD)
    }

    private fun bindCheckoutRow(row: CheckoutRow) {
        binding.productName.text = row.products.first().name
        binding.productPrice.text = formatter.formatPrice(row.products.first().price)
        binding.productQuantity.text = row.products.size.toString()
        binding.amount.text = formatter.formatPrice(row.amount)
        binding.discount.text = formatter.formatPercent(row.discountedPercent)
    }
}