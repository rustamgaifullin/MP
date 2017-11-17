package io.rg.mp.utils

import android.content.Context
import android.net.ConnectivityManager


fun Context.isDeviceOnline(): Boolean {
    val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = connMgr.activeNetworkInfo
    return networkInfo != null && networkInfo.isConnected
}