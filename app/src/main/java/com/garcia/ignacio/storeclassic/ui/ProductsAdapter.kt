package com.garcia.ignacio.storeclassic.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.garcia.ignacio.storeclassic.R
import com.garcia.ignacio.storeclassic.databinding.ProductListItemBinding
import com.garcia.ignacio.storeclassic.domain.models.Product
import java.text.DecimalFormat
import javax.inject.Inject

private const val PRICE_FORMAT = "0.#"

class ProductsAdapter @Inject constructor() :
    ListAdapter<Product, ProductsAdapter.ProductViewHolder>(ProductDiffCallback()) {

    class ProductViewHolder(
        private val binding: ProductListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private val priceFormatter = DecimalFormat(PRICE_FORMAT)
        fun bind(product: Product) {
            binding.productName.text = product.name
            binding.productPrice.text = itemView.context.getString(
                R.string.euro_currency_format, priceFormatter.format(product.price)
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        return ProductViewHolder(
            ProductListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
    override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem.code == newItem.code
    }

    override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem == newItem
    }
}