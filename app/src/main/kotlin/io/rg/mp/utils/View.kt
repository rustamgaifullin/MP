package io.rg.mp.utils

import android.view.View

fun View.setVisibility(visibility: Boolean) {
    val visibilityConstant = if (visibility) View.VISIBLE else View.INVISIBLE
    setVisibility(visibilityConstant)
}