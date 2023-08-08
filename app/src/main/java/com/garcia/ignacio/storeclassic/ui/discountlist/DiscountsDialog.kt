package com.garcia.ignacio.storeclassic.ui.discountlist

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
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

    @Inject
    lateinit var discountsAdapter: DiscountsAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(
            requireContext()
        )
        _binding = DialogDiscountsBinding.inflate(layoutInflater, null, false)
        builder.setView(binding.root)
        builder.setTitle(R.string.discounts_dialog_label)
        return builder.create()
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

        initializeRecyclerView()
        observeViewModel()
    }

    private fun initializeRecyclerView() {
        binding.discountList.adapter = discountsAdapter
    }

    private fun observeViewModel() {
        viewModel.discountedProducts.observe(viewLifecycleOwner) {
            discountsAdapter.submitList(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}