package com.garcia.ignacio.storeclassic.ui.productlist

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.garcia.ignacio.storeclassic.R
import com.garcia.ignacio.storeclassic.databinding.FragmentProductListBinding
import com.garcia.ignacio.storeclassic.domain.models.Product
import com.garcia.ignacio.storeclassic.ui.StoreViewModel
import com.garcia.ignacio.storeclassic.ui.dialog.ConfirmationDialog
import com.garcia.ignacio.storeclassic.ui.dialog.showConfirmationDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val ADD_TO_CART_CONFIRMATION_DIALOG = "AddToCartConfirmation"

@AndroidEntryPoint
class ProductListFragment : Fragment() {

    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StoreViewModel by viewModels()

    @Inject
    lateinit var productsAdapter: ProductsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        childFragmentManager.setFragmentResultListener(
            ADD_TO_CART_CONFIRMATION_DIALOG,
            requireActivity()
        ) { _, bundle ->
            val addToCartConfirmationResult = bundle.getInt(ConfirmationDialog.RESULT_KEY)
            processAddToCartConfirmationResult(addToCartConfirmationResult)
        }
    }

    private fun processAddToCartConfirmationResult(addToCartConfirmationResult: Int) {
        when (addToCartConfirmationResult) {
            DialogInterface.BUTTON_POSITIVE -> addToCartConfirmed()
            DialogInterface.BUTTON_NEGATIVE,
            ConfirmationDialog.CANCELLED -> addToCartCancelled()

            else -> error("unexpected result: $addToCartConfirmationResult")
        }
    }

    private fun addToCartCancelled() {
        viewModel.pendingAddToCartCancelled()
    }

    private fun addToCartConfirmed() {
        viewModel.pendingAddToCartConfirmed()
    }

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
                    showAddToCartConfirmationDialog(it.product, it.quantity)
                }

            StoreViewModel.Effect.Idle -> {
                // NOP
            }
        }
    }

    private fun showAddToCartConfirmationDialog(product: Product, quantity: Int) {
        val message = resources.getQuantityString(
            R.plurals.add_to_cart_confirmation_message, quantity, quantity, product.name
        )
        showConfirmationDialog(
            tag = ADD_TO_CART_CONFIRMATION_DIALOG,
            title = getString(R.string.add_to_cart_confirmation_title),
            message = HtmlCompat.fromHtml(message, HtmlCompat.FROM_HTML_MODE_COMPACT),
            confirmText = getString(R.string.add_to_cart),
            cancelText = getString(R.string.cancel),
        )
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