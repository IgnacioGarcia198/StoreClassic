package com.garcia.ignacio.storeclassic.ui.discountlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.garcia.ignacio.storeclassic.databinding.DiscountListItemBinding
import com.garcia.ignacio.storeclassic.ui.model.UiDiscountedProduct
import javax.inject.Inject

class DiscountsAdapter @Inject constructor() :
    ListAdapter<UiDiscountedProduct, DiscountViewHolder>(DiscountDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscountViewHolder {
        val binding = DiscountListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DiscountViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DiscountViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class DiscountDiffCallback : DiffUtil.ItemCallback<UiDiscountedProduct>() {
    override fun areItemsTheSame(
        oldItem: UiDiscountedProduct,
        newItem: UiDiscountedProduct
    ): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(
        oldItem: UiDiscountedProduct,
        newItem: UiDiscountedProduct
    ): Boolean {
        return oldItem == newItem
    }
}