package com.garcia.ignacio.storeclassic.ui.checkout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.garcia.ignacio.storeclassic.databinding.FragmentCheckoutBinding
import com.garcia.ignacio.storeclassic.ui.model.ListState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CheckoutFragment : Fragment() {

    private var _binding: FragmentCheckoutBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CheckoutViewModel by viewModels()

    @Inject
    lateinit var adapter: CheckoutAdapter

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
        viewModel.getCheckoutState().observe(viewLifecycleOwner) {
            renderState(it)
        }
    }

    private fun renderState(state: ListState<CheckoutRow>) {
        when (state) {
            ListState.Loading -> {
                showLoading()
            }

            is ListState.Ready -> {
                hideLoading()
                renderCheckoutData(state.list)
            }
        }
    }

    private fun hideLoading() {
        binding.loading.hide()
    }

    private fun showLoading() {
        binding.loading.show()
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