package com.garcia.ignacio.storeclassic.ui.discountlist

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.garcia.ignacio.storeclassic.R
import com.garcia.ignacio.storeclassic.databinding.DialogDiscountsBinding
import com.garcia.ignacio.storeclassic.ui.StoreViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DiscountsDialog : DialogFragment() {
    private var _binding: DialogDiscountsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StoreViewModel by activityViewModels()
    private val productCode: String? by lazy { arguments?.getString(ARG_PRODUCT_CODE) }
    private val productName: String? by lazy { arguments?.getString(ARG_PRODUCT_NAME) }

    @Inject
    lateinit var discountsAdapter: DiscountsAdapter

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
        if (savedInstanceState == null) {
            viewModel.computeDiscountsForProduct(productCode)
        }
    }

    private fun initializeRecyclerView() {
        binding.discountList.adapter = discountsAdapter
    }

    private fun observeViewModel() {
        viewModel.getDiscountsForCurrentProduct().observe(viewLifecycleOwner) { list ->
            discountsAdapter.submitList(list)
            val emptyDiscounts = list.isEmpty()
            binding.noDiscountsText.isVisible = emptyDiscounts
            binding.productHeader.isVisible = !emptyDiscounts
            binding.discountHeader.isVisible = !emptyDiscounts
        }
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