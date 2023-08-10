package com.garcia.ignacio.storeclassic.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.garcia.ignacio.storeclassic.databinding.MessageProgressBarBinding

class MessageProgressBar(
    context: Context,
    attrs: AttributeSet?
) : LinearLayout(context, attrs) {
    private val binding = MessageProgressBarBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    fun show() {
        binding.loading.show()
        binding.loadingText.isVisible = true
    }

    fun hide() {
        binding.loading.hide()
        binding.loadingText.isVisible = false
    }
}