package com.garcia.ignacio.storeclassic.ui.checkout

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.garcia.ignacio.storeclassic.databinding.CheckoutItemBinding
import com.garcia.ignacio.storeclassic.ui.formatting.StoreFormatter
import javax.inject.Inject

class CheckoutAdapter @Inject constructor(
    private val formatter: StoreFormatter,
) : ListAdapter<CheckoutRow, CheckoutViewHolder>(CheckoutDiffCallback()) {
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

class CheckoutDiffCallback : DiffUtil.ItemCallback<CheckoutRow>() {
    override fun areItemsTheSame(oldItem: CheckoutRow, newItem: CheckoutRow): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: CheckoutRow, newItem: CheckoutRow): Boolean {
        return oldItem == newItem
    }
}