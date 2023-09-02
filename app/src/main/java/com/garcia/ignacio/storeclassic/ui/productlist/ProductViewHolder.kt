package com.garcia.ignacio.storeclassic.ui.productlist

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import com.garcia.ignacio.storeclassic.databinding.ProductListItemBinding
import com.garcia.ignacio.storeclassic.domain.models.DiscountedProduct
import com.garcia.ignacio.storeclassic.ui.formatting.StoreFormatter

private const val ADD_TO_CART_AT_ONCE_LIMIT = 10

class ProductViewHolder(
    private val binding: ProductListItemBinding,
    private val actions: ProductItemActions,
    private val formatter: StoreFormatter,
) : RecyclerView.ViewHolder(binding.root) {

    init {
        ArrayAdapter(
            itemView.context,
            android.R.layout.simple_spinner_item,
            (1..ADD_TO_CART_AT_ONCE_LIMIT).toMutableList()
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.addToCart.adapter = adapter
        }
    }

    fun bind(product: DiscountedProduct) {
        binding.seeDiscounts.visibility =
            if (product.discount != null) View.VISIBLE
            else View.INVISIBLE
        binding.seeDiscounts.setOnClickListener {
            actions.seeDiscounts(product.product)
        }
        binding.addToCart.setSelection(binding.addToCart.selectedItemPosition, false)
        binding.addToCart.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                actions.productQuantitySelected(product.product, position + 1)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // NOP
            }
        }
        binding.productName.text = product.product.name
        binding.productPrice.text = formatter.formatPrice(product.product.price)
    }
}