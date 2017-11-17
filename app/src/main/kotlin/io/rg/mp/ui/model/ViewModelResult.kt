package io.rg.mp.ui.model

import android.content.Intent

sealed class ViewModelResult

data class ToastInfo(val messageId: Int, val length: Int) : ViewModelResult()
data class StartActivity(val intent: Intent, val requestCode: Int) : ViewModelResult()
data class PermissionRequest(val permissions: Array<String>, val requestCode: Int) : ViewModelResult()
data class GooglePlayServicesAvailabilityError(val requestCode: Int) : ViewModelResult()