package com.garcia.ignacio.storeclassic.ui.productlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.garcia.ignacio.storeclassic.databinding.FragmentProductListBinding
import com.garcia.ignacio.storeclassic.ui.StoreViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProductListFragment : Fragment() {

    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StoreViewModel by viewModels()

    @Inject
    lateinit var productsAdapter: ProductsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeRecyclerView()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.getState().observe(viewLifecycleOwner) {
            renderState(it)
        }
        viewModel.getEffect().observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                renderEffect(it)
            }
        }
    }

    private fun renderState(state: StoreViewModel.State) {
        when (state) {
            StoreViewModel.State.Loading -> {
                showLoading()
            }

            is StoreViewModel.State.Ready -> {
                hideLoading()
                productsAdapter.submitList(state.products)
                binding.noProductsText.isVisible = state.products.isEmpty()
            }
        }
    }

    private fun hideLoading() {
        binding.loading.hide()
        binding.loadingText.isVisible = false
    }

    private fun showLoading() {
        binding.loading.show()
        binding.loadingText.isVisible = true
    }

    private fun renderEffect(effect: StoreViewModel.Effect) {
        when (effect) {
            StoreViewModel.Effect.AddToCartConfirmation ->
                viewModel.pendingAddToCart?.let {
                    showAddToCartConfirmationDialog()
                }

            StoreViewModel.Effect.Idle -> {
                // NOP
            }
        }
    }

    private fun showAddToCartConfirmationDialog() {

    }

    private fun initializeRecyclerView() {
        binding.productList.adapter = productsAdapter.also {
            it.initialize(viewModel)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}