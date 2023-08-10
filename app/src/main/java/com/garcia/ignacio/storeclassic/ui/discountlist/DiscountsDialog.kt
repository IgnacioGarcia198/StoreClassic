package com.garcia.ignacio.storeclassic.ui.discountlist

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.garcia.ignacio.storeclassic.R
import com.garcia.ignacio.storeclassic.databinding.DialogDiscountsBinding
import com.garcia.ignacio.storeclassic.domain.models.DiscountedProduct
import com.garcia.ignacio.storeclassic.ui.model.ListState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DiscountsDialog : DialogFragment() {
    private var _binding: DialogDiscountsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DiscountsViewModel by viewModels()
    private val productCode: String? by lazy { arguments?.getString(ARG_PRODUCT_CODE) }
    private val productName: String? by lazy { arguments?.getString(ARG_PRODUCT_NAME) }

    @Inject
    lateinit var discountsAdapter: DiscountsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeViewModel(savedInstanceState)
    }

    private fun initializeViewModel(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            viewModel.initialize(productCode)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(
            requireContext()
        ).setPositiveButton(R.string.close) { _, _ ->
            dismiss()
        }.setView(
            DialogDiscountsBinding.inflate(layoutInflater, null, false)
                .also { _binding = it }.root
        ).create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setTitle(
            productName
                ?.let { getString(R.string.discounts_for_chosen_product_title, it) }
                ?: getString(R.string.discounts_dialog_label)
        )

        initializeRecyclerView()
        observeViewModel()
    }

    private fun initializeRecyclerView() {
        binding.discountList.adapter = discountsAdapter
    }

    private fun observeViewModel() {
        viewModel.getDiscountsState().observe(viewLifecycleOwner) {
            renderState(it)
        }
    }

    private fun renderState(state: ListState<DiscountedProduct>) {
        when (state) {
            ListState.Loading -> {
                showLoading()
            }

            is ListState.Ready -> {
                hideLoading()
                renderDiscounts(state.list)
            }
        }
    }

    private fun hideLoading() {
        binding.loading.hide()
    }

    private fun showLoading() {
        binding.loading.show()
    }

    private fun renderDiscounts(list: List<DiscountedProduct>) {
        discountsAdapter.submitList(list)
        val emptyDiscounts = list.isEmpty()
        binding.noDiscountsText.isVisible = emptyDiscounts
        binding.productHeader.isVisible = !emptyDiscounts
        binding.discountHeader.isVisible = !emptyDiscounts
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_PRODUCT_CODE = "DiscountDialog.productCode"
        const val ARG_PRODUCT_NAME = "DiscountDialog.productName"
    }
}