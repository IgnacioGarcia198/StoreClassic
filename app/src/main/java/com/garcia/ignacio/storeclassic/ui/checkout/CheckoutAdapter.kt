package com.garcia.ignacio.storeclassic.ui.checkout

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.garcia.ignacio.storeclassic.databinding.CheckoutItemBinding
import com.garcia.ignacio.storeclassic.ui.formatting.StoreFormatter
import com.garcia.ignacio.storeclassic.ui.model.UiCheckoutRow
import javax.inject.Inject

class CheckoutAdapter @Inject constructor(
    private val formatter: StoreFormatter,
) : ListAdapter<UiCheckoutRow, CheckoutViewHolder>(CheckoutDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckoutViewHolder {
        val binding = CheckoutItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CheckoutViewHolder(binding, formatter)
    }

    override fun onBindViewHolder(holder: CheckoutViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class CheckoutDiffCallback : DiffUtil.ItemCallback<UiCheckoutRow>() {
    override fun areItemsTheSame(oldItem: UiCheckoutRow, newItem: UiCheckoutRow): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: UiCheckoutRow, newItem: UiCheckoutRow): Boolean {
        return oldItem == newItem
    }
}