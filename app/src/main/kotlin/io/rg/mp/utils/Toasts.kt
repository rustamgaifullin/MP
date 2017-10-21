package io.rg.mp.utils

import android.content.Context
import android.widget.Toast
import io.rg.mp.R

class Toasts {
    fun noNetwork(context: Context) {
        shortToast(context, context.getString(R.string.no_network_message))
    }

    fun requireGooglePlayServices(context: Context) {
        longToast(context, context.getString(R.string.requre_google_play_services))
    }

    fun shortToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun longToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}