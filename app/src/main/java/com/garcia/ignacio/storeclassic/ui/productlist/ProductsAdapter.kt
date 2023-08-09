package com.garcia.ignacio.storeclassic.ui.productlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.garcia.ignacio.storeclassic.databinding.ProductListItemBinding
import com.garcia.ignacio.storeclassic.domain.models.Product
import com.garcia.ignacio.storeclassic.ui.StoreViewModel
import com.garcia.ignacio.storeclassic.ui.formatting.StoreFormatter
import javax.inject.Inject

class ProductsAdapter @Inject constructor(
    private val formatter: StoreFormatter,
) : ListAdapter<Product, ProductViewHolder>(ProductDiffCallback()) {

    lateinit var viewModel: StoreViewModel

    fun initialize(viewModel: StoreViewModel) {
        this.viewModel = viewModel
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ProductListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProductViewHolder(
            binding,
            viewModel,
            formatter
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