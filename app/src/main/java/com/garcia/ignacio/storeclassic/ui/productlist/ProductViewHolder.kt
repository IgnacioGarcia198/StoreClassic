package com.garcia.ignacio.storeclassic.ui.productlist

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import com.garcia.ignacio.storeclassic.databinding.ProductListItemBinding
import com.garcia.ignacio.storeclassic.domain.models.Product
import com.garcia.ignacio.storeclassic.ui.StoreViewModel
import java.text.DecimalFormat

private const val PRICE_FORMAT = "0.#"

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
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.addToCart.adapter = adapter
        }
    }

    fun bind(product: Product) {
        binding.seeDiscounts.setOnClickListener { println("discounts clicked") }
        binding.addToCart.setSelection(binding.addToCart.selectedItemPosition, false)
        binding.addToCart.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
            com.garcia.ignacio.storeclassic.R.string.euro_currency_format,
            priceFormatter.format(product.price)
        )
    }
}