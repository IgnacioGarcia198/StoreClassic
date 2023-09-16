package com.garcia.ignacio.storeclassic.ui.discountlist

import androidx.recyclerview.widget.RecyclerView
import com.garcia.ignacio.storeclassic.databinding.DiscountListItemBinding
import com.garcia.ignacio.storeclassic.domain.models.DiscountedProduct
import com.garcia.ignacio.storeclassic.ui.extensions.expressAsString
import com.garcia.ignacio.storeclassic.ui.model.UiDiscountedProduct

class DiscountViewHolder(
    private val binding: DiscountListItemBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(discountedProduct: UiDiscountedProduct) {
        binding.productName.text = discountedProduct.name
        binding.discountText.text = discountedProduct.discountString
    }
}