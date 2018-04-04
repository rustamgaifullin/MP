package io.rg.mp.ui.extension

import android.view.View

fun View.setVisibility(visibility: Boolean) {
    val visibilityConstant = if (visibility) View.VISIBLE else View.INVISIBLE
    setVisibility(visibilityConstant)
}