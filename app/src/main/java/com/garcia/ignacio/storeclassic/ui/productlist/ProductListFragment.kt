package com.garcia.ignacio.storeclassic.ui.productlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        viewModel.getProducts().observe(viewLifecycleOwner) {
            productsAdapter.submitList(it)
        }
        viewModel.getEffect().observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                renderEffect(it)
            }
        }
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
        TODO("Not yet implemented")
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