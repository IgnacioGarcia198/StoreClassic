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
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CheckoutFragment : Fragment() {

    private var _binding: FragmentCheckoutBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StoreViewModel by activityViewModels()

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
        viewModel.getCheckoutRows().observe(viewLifecycleOwner) { checkoutRows ->
            val emptyCart = checkoutRows.isEmpty()
            binding.checkoutGroup.isVisible = !emptyCart
            binding.emptyCartText.isVisible = emptyCart
            adapter.submitList(checkoutRows)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}