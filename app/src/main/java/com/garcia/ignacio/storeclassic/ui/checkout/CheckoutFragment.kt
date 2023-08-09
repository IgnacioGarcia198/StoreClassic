package com.garcia.ignacio.storeclassic.ui.checkout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.garcia.ignacio.storeclassic.databinding.FragmentCheckoutBinding
import com.garcia.ignacio.storeclassic.ui.StoreViewModel
import com.garcia.ignacio.storeclassic.ui.formatting.StoreFormatter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

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

    private fun renderCheckoutData(checkoutData: List<CheckoutRow>) {
        val emptyCart = checkoutData.isEmpty()
        binding.checkoutGroup.isVisible = !emptyCart
        binding.emptyCartText.isVisible = emptyCart
        binding.clearCart.isVisible = !emptyCart
        if (!emptyCart) {
            binding.clearCart.setOnClickListener {
                viewModel.clearCart()
            }
        }
        adapter.submitList(checkoutData)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}