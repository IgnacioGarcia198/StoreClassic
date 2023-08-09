package com.garcia.ignacio.storeclassic.ui.checkout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.garcia.ignacio.storeclassic.R
import com.garcia.ignacio.storeclassic.databinding.FragmentCheckoutBinding
import com.garcia.ignacio.storeclassic.ui.StoreViewModel
import com.garcia.ignacio.storeclassic.ui.formatting.StoreFormatter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val NO_VALUE = "----"
@AndroidEntryPoint
class CheckoutFragment : Fragment() {

    private var _binding: FragmentCheckoutBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StoreViewModel by activityViewModels()

    @Inject
    lateinit var adapter: CheckoutAdapter
    @Inject
    lateinit var formatter: StoreFormatter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCheckoutBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeRecyclerView()
        observeViewModel()
    }

    private fun initializeRecyclerView() {
        binding.checkoutList.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.getCheckoutData().observe(viewLifecycleOwner) { checkoutData ->
            renderCheckoutData(checkoutData)
        }
        viewModel.computeCheckoutData()
    }

    private fun renderCheckoutData(checkoutData: CheckoutData) {
        val emptyCart = checkoutData.checkoutRows.isEmpty()
        binding.checkoutGroup.isVisible = !emptyCart
        binding.emptyCartText.isVisible = emptyCart
        if (!emptyCart) {
            adapter.submitList(checkoutData.checkoutRows)
            val totalsBinding = binding.totals
            totalsBinding.productName.text = getString(R.string.checkout_total)
            totalsBinding.productPrice.text = NO_VALUE
            totalsBinding.productQuantity.text = checkoutData.totalQuantity.toString()
            totalsBinding.amount.text = formatter.formatPrice(checkoutData.totalAmount)
            totalsBinding.discount.text = formatter.formatPercent(checkoutData.totalDiscount)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}