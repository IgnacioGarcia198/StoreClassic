package com.garcia.ignacio.storeclassic.ui.customview

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatSpinner

class AlwaysChangeSpinner(
    context: Context,
    attrs: AttributeSet
) : AppCompatSpinner(context, attrs) {
    override fun setSelection(position: Int) {
        val sameSelection = position == selectedItemPosition
        super.setSelection(position)
        if (sameSelection) {
            onItemSelectedListener?.onItemSelected(null, null, position, 0)
        }
    }
}