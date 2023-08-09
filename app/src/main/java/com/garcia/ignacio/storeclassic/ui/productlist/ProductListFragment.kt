package com.garcia.ignacio.storeclassic.ui.productlist

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.garcia.ignacio.storeclassic.BuildConfig
import com.garcia.ignacio.storeclassic.R
import com.garcia.ignacio.storeclassic.data.exceptions.StoreException
import com.garcia.ignacio.storeclassic.databinding.FragmentProductListBinding
import com.garcia.ignacio.storeclassic.domain.models.Product
import com.garcia.ignacio.storeclassic.ui.StoreViewModel
import com.garcia.ignacio.storeclassic.ui.dialog.ConfirmationDialog
import com.garcia.ignacio.storeclassic.ui.dialog.showConfirmationDialog
import com.garcia.ignacio.storeclassic.ui.discountlist.DiscountsDialog
import com.garcia.ignacio.storeclassic.ui.exceptions.ReportableError
import com.garcia.ignacio.storeclassic.ui.model.AddToCart
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val ADD_TO_CART_CONFIRMATION_DIALOG = "AddToCartConfirmation"
private const val DEVELOPER_EMAIL = "ignaciogarcia198@gmail.com"
private const val EMAIL_MIME_TYPE = "message/rfc822"
private const val ERROR_FEEDBACK_MAX_LINES = 10
private const val ERROR_FEEDBACK_DURATION = 4000

@AndroidEntryPoint
class ProductListFragment : Fragment() {

    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StoreViewModel by activityViewModels()

    @Inject
    lateinit var productsAdapter: ProductsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(
            ADD_TO_CART_CONFIRMATION_DIALOG
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

            else ->
                if (BuildConfig.DEBUG) {
                    throw StoreException.Misusing("unexpected result: $addToCartConfirmationResult")
                }
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
        binding.checkoutFab.setOnClickListener {
            viewModel.goToCheckout()
        }
    }

    private fun observeViewModel() {
        viewModel.getProductsState().observe(viewLifecycleOwner) {
            renderState(it)
        }
        viewModel.getProductsEffect().observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                renderEffect(it)
            }
        }
    }

    private fun renderState(state: ProductsState) {
        when (state) {
            ProductsState.Loading -> {
                showLoading()
            }

            is ProductsState.Ready -> {
                hideLoading()
                productsAdapter.submitList(state.products)
                binding.noProductsText.isVisible = state.products.isEmpty()
            }
        }
    }

    private fun renderEffect(effect: ProductsEffect) {
        when (effect) {
            ProductsEffect.AddToCartConfirmation ->
                viewModel.pendingAddToCart?.let {
                    showAddToCartConfirmationDialog(it.product, it.quantity)
                }

            ProductsEffect.Idle -> {
                // NOP
            }

            is ProductsEffect.AddToCartConfirmed ->
                showAddedToCartFeedback(effect.addToCart)

            is ProductsEffect.ReportErrors ->
                showErrorsFeedback(effect.compoundError)

            is ProductsEffect.DisplayDiscounts ->
                displayDiscountsForProduct(effect.product)

            ProductsEffect.Checkout ->
                findNavController().navigate(R.id.action_ProductsFragment_to_CheckoutFragment)
        }
    }

    private fun displayDiscountsForProduct(product: Product) {
        findNavController().navigate(
            R.id.action_ProductsFragment_to_discountsDialog,
            bundleOf(
                DiscountsDialog.ARG_PRODUCT_CODE to product.code,
                DiscountsDialog.ARG_PRODUCT_NAME to product.name,
            )
        )
    }

    private fun showErrorsFeedback(compoundError: ReportableError) {
        Snackbar.make(
            requireView(),
            getString(R.string.error_feedback_title, compoundError.errorMessage),
            ERROR_FEEDBACK_DURATION
        ).setAction(getString(R.string.error_feedback_report_action)) {
            reportError(compoundError)
        }.setActionTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.errorFeedbackReportActionColor
            )
        ).setTextColor(
            ContextCompat.getColor(requireContext(), R.color.white)
        ).setBackgroundTint(
            ContextCompat.getColor(
                requireContext(),
                R.color.errorFeedbackBackground
            )
        ).setTextMaxLines(
            ERROR_FEEDBACK_MAX_LINES
        ).show()
    }

    private fun reportError(compoundError: ReportableError) {
        val email = Intent(Intent.ACTION_SEND)
        email.putExtra(Intent.EXTRA_EMAIL, arrayOf(DEVELOPER_EMAIL))
        email.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.error_report_email_title))
        email.putExtra(Intent.EXTRA_TEXT, compoundError.reportMessage)
        email.type = EMAIL_MIME_TYPE

        startActivity(
            Intent.createChooser(
                email,
                getString(R.string.error_report_mail_client_chooser_title)
            )
        )
    }

    private fun showAddedToCartFeedback(addToCart: AddToCart) {
        val feedbackText = resources.getQuantityString(
            R.plurals.added_to_cart_feedback,
            addToCart.quantity,
            addToCart.quantity,
            addToCart.product.name
        )
        val feedbackHtml = HtmlCompat.fromHtml(feedbackText, HtmlCompat.FROM_HTML_MODE_COMPACT)
        Snackbar.make(requireView(), feedbackHtml, Snackbar.LENGTH_SHORT)
            .setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            .setBackgroundTint(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.addedToCartFeedbackBackground
                )
            ).show()
    }

    private fun hideLoading() {
        binding.loading.hide()
        binding.loadingText.isVisible = false
    }

    private fun showLoading() {
        binding.loading.show()
        binding.loadingText.isVisible = true
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