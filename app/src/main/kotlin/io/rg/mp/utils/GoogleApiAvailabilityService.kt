package io.rg.mp.utils

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

class GoogleApiAvailabilityService(
        val context: Context,
        val apiAvailability: GoogleApiAvailability) {

    fun isAvailable(): Boolean {
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(context)
        return connectionStatusCode == ConnectionResult.SUCCESS
    }

    fun acquire(result:(connectionStatusCode: Int) -> Unit) {
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(context)
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            result(connectionStatusCode)
        }
    }
}