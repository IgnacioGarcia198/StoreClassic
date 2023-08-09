package com.garcia.ignacio.storeclassic.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.garcia.ignacio.storeclassic.R
import com.garcia.ignacio.storeclassic.databinding.ActivityMainBinding
import com.garcia.ignacio.storeclassic.ui.exceptions.ReportableError
import com.garcia.ignacio.storeclassic.ui.productlist.AppEffect
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

private const val DEVELOPER_EMAIL = "ignaciogarcia198@gmail.com"
private const val EMAIL_MIME_TYPE = "message/rfc822"
private const val ERROR_FEEDBACK_MAX_LINES = 10
private const val ERROR_FEEDBACK_DURATION = 4000

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val viewModel: StoreViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.getAppEffect().observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                renderEffect(it)
            }
        }
    }

    private fun renderEffect(effect: AppEffect) {
        when (effect) {
            is AppEffect.ReportErrors ->
                showErrorsFeedback(effect.compoundError)

            AppEffect.ConnectionRestored ->
                showConnectionRestoredFeedback()

            else -> {
                // NOP
            }
        }
    }

    private fun showConnectionRestoredFeedback() {
        val feedbackText = getString(R.string.connection_restored_feedback)
        Snackbar.make(binding.root, feedbackText, Snackbar.LENGTH_SHORT)
            .setTextColor(ContextCompat.getColor(this, R.color.white))
            .setBackgroundTint(
                ContextCompat.getColor(
                    this,
                    R.color.addedToCartFeedbackBackground
                )
            ).show()
    }

    private fun showErrorsFeedback(compoundError: ReportableError) {
        Snackbar.make(
            binding.root,
            getString(R.string.error_feedback_title, compoundError.errorMessage),
            ERROR_FEEDBACK_DURATION
        ).setAction(getString(R.string.error_feedback_report_action)) {
            reportError(compoundError)
        }.setActionTextColor(
            ContextCompat.getColor(
                this,
                R.color.errorFeedbackReportActionColor
            )
        ).setTextColor(
            ContextCompat.getColor(this, R.color.white)
        ).setBackgroundTint(
            ContextCompat.getColor(
                this,
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.piggy_bank) {
            findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.discountsDialog)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}