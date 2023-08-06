package com.garcia.ignacio.storeclassic.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment

private const val ARG_TAG = "dialogTag"
private const val ARG_TITLE = "title"
private const val ARG_MESSAGE = "message"
private const val ARG_CONFIRM_TEXT = "confirm"
private const val ARG_CANCEL_TEXT = "cancel"
private const val ARG_NEUTRAL_TEXT = "neutral"

class ConfirmationDialog : DialogFragment(), DialogInterface.OnClickListener {

    private lateinit var listener: Listener
    private val dialogTag: String by lazy {
        arguments?.getString(ARG_TAG) ?: error("tag argument needed")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listener = when {
            parentFragment is Listener -> parentFragment as Listener
            activity is Listener -> activity as Listener
            else -> error(
                "Either Activity or parent Fragment must implement " +
                        "ConfirmationDialog.Listener"
            )
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(
            requireContext()
        )
        builder.setTitle(arguments?.getCharSequence(ARG_TITLE))
        builder.setMessage(arguments?.getCharSequence(ARG_MESSAGE))
        arguments?.getCharSequence(ARG_CONFIRM_TEXT)?.let { confirmText ->
            builder.setPositiveButton(confirmText, this)
        }
        arguments?.getCharSequence(ARG_CANCEL_TEXT)?.let { cancelText ->
            builder.setNegativeButton(cancelText, this)
        }
        arguments?.getCharSequence(ARG_NEUTRAL_TEXT)?.let { neutralText ->
            builder.setNeutralButton(neutralText, this)
        }
        return builder.create()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        listener.dialogCancelled(dialogTag)
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                listener.doPositiveClick(dialogTag)
            }

            DialogInterface.BUTTON_NEGATIVE -> {
                listener.doNegativeClick(dialogTag)
            }

            DialogInterface.BUTTON_NEUTRAL -> {
                listener.doNeutralClick(dialogTag)
            }
        }
    }

    interface Listener {
        fun doPositiveClick(dialogTag: String)
        fun doNegativeClick(dialogTag: String)
        fun doNeutralClick(dialogTag: String)
        fun dialogCancelled(dialogTag: String)
    }
}

fun AppCompatActivity.showConfirmationDialog(
    tag: String,
    title: CharSequence,
    message: CharSequence,
    confirmText: CharSequence? = null,
    cancelText: CharSequence? = null,
    neutralText: CharSequence? = null,
) {
    assert(this is ConfirmationDialog.Listener) {
        "${javaClass.simpleName} must implement " +
                "ConfirmationDialog.Listener"
    }
    val fragment = newInstance(tag, title, message, confirmText, cancelText, neutralText)
    supportFragmentManager
        .beginTransaction()
        .add(fragment, tag)
        .commitAllowingStateLoss()
}

private fun newInstance(
    tag: String,
    title: CharSequence,
    message: CharSequence,
    confirmText: CharSequence?,
    cancelText: CharSequence?,
    neutralText: CharSequence?
): ConfirmationDialog = ConfirmationDialog().apply {
    arguments = bundleOf(
        ARG_TAG to tag,
        ARG_TITLE to title,
        ARG_MESSAGE to message,
        ARG_CONFIRM_TEXT to confirmText,
        ARG_CANCEL_TEXT to cancelText,
        ARG_NEUTRAL_TEXT to neutralText
    )
}


fun Fragment.showConfirmationDialog(
    tag: String,
    title: CharSequence,
    message: CharSequence,
    confirmText: CharSequence? = null,
    cancelText: CharSequence? = null,
    neutralText: CharSequence? = null,
) {
    assert(this is ConfirmationDialog.Listener) {
        "${javaClass.simpleName} must implement " +
                "ConfirmationDialog.Listener"
    }
    val fragment = newInstance(tag, title, message, confirmText, cancelText, neutralText)
    childFragmentManager
        .beginTransaction()
        .add(fragment, tag)
        .commitAllowingStateLoss()
}