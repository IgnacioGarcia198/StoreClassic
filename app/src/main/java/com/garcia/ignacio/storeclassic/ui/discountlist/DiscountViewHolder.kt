package com.garcia.ignacio.storeclassic.ui.discountlist

import androidx.recyclerview.widget.RecyclerView
import com.garcia.ignacio.storeclassic.databinding.DiscountListItemBinding

class DiscountViewHolder(
    private val binding: DiscountListItemBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(discountedProduct: DiscountedProduct) {
        binding.productName.text = discountedProduct.productName
        binding.discountText.text = discountedProduct.discountText
    }
}