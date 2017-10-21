package io.rg.mp.utils

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability


fun Activity.isDeviceOnline(): Boolean {
    val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = connMgr.activeNetworkInfo
    return networkInfo != null && networkInfo.isConnected
}

fun Activity.isGooglePlayServicesAvailable(): Boolean {
    val apiAvailability = GoogleApiAvailability.getInstance()
    val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
    return connectionStatusCode == ConnectionResult.SUCCESS
}

fun Activity.acquireGooglePlayServices(result:(activity: Activity, connectionStatusCode: Int) -> Unit) {
    val apiAvailability = GoogleApiAvailability.getInstance()
    val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
    if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
        result(this, connectionStatusCode)
    }
}