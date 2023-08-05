package com.garcia.ignacio.storeclassic.ui.productlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.garcia.ignacio.storeclassic.R
import com.garcia.ignacio.storeclassic.databinding.ProductListItemBinding
import com.garcia.ignacio.storeclassic.domain.models.Product
import com.garcia.ignacio.storeclassic.ui.StoreViewModel
import java.text.DecimalFormat
import javax.inject.Inject

private const val PRICE_FORMAT = "0.#"

class ProductsAdapter @Inject constructor() :
    ListAdapter<Product, ProductsAdapter.ProductViewHolder>(ProductDiffCallback()) {

    lateinit var viewModel: StoreViewModel

    fun initialize(viewModel: StoreViewModel) {
        this.viewModel = viewModel
    }

    class ProductViewHolder(
        private val binding: ProductListItemBinding,
        private val viewModel: StoreViewModel
    ) : RecyclerView.ViewHolder(binding.root) {
        private val priceFormatter = DecimalFormat(PRICE_FORMAT)

        init {
            ArrayAdapter(
                itemView.context,
                android.R.layout.simple_spinner_item,
                (1..10).toMutableList()
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_item)
                binding.addToCart.adapter = adapter
            }
        }

        fun bind(product: Product) {
            binding.seeDiscounts.setOnClickListener { println("discounts clicked") }
            binding.addToCart.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    viewModel.pendingAddToCart(product, position + 1)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // NOP
                }
            }
            binding.productName.text = product.name
            binding.productPrice.text = itemView.context.getString(
                R.string.euro_currency_format, priceFormatter.format(product.price)
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ProductListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProductViewHolder(
            binding,
            viewModel
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