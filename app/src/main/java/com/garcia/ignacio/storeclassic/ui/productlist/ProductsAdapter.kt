package com.garcia.ignacio.storeclassic.ui.productlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.garcia.ignacio.storeclassic.databinding.ProductListItemBinding
import com.garcia.ignacio.storeclassic.domain.models.DiscountedProduct
import com.garcia.ignacio.storeclassic.ui.formatting.StoreFormatter
import javax.inject.Inject

class ProductsAdapter @Inject constructor(
    private val formatter: StoreFormatter,
) : ListAdapter<DiscountedProduct, ProductViewHolder>(ProductDiffCallback()) {

    lateinit var actions: ProductItemActions

    fun initialize(actions: ProductItemActions) {
        this.actions = actions
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ProductListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProductViewHolder(
            binding,
            actions,
            formatter
        )
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class ProductDiffCallback : DiffUtil.ItemCallback<DiscountedProduct>() {
    override fun areItemsTheSame(oldItem: DiscountedProduct, newItem: DiscountedProduct): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(
        oldItem: DiscountedProduct,
        newItem: DiscountedProduct
    ): Boolean {
        return oldItem == newItem
    }
}