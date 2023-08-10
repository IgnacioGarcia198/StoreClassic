package com.garcia.ignacio.storeclassic.ui.discountlist

import androidx.recyclerview.widget.RecyclerView
import com.garcia.ignacio.storeclassic.databinding.DiscountListItemBinding
import com.garcia.ignacio.storeclassic.domain.models.DiscountedProduct
import com.garcia.ignacio.storeclassic.ui.extensions.expressAsString

class DiscountViewHolder(
    private val binding: DiscountListItemBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(discountedProduct: DiscountedProduct) {
        binding.productName.text = discountedProduct.product.name
        binding.discountText.text = discountedProduct.discount?.expressAsString(itemView.context)
    }
}