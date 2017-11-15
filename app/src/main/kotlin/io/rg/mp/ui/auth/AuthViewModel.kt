package io.rg.mp.ui.auth

import android.Manifest
import android.Manifest.permission.GET_ACCOUNTS
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.widget.Toast.LENGTH_LONG
import android.widget.Toast.LENGTH_SHORT
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.rg.mp.R
import io.rg.mp.service.drive.SpreadsheetService
import io.rg.mp.ui.model.GooglePlayServicesAvailabilityError
import io.rg.mp.ui.model.PermissionRequest
import io.rg.mp.ui.model.StartActivity
import io.rg.mp.ui.model.ToastInfo
import io.rg.mp.ui.model.ViewModelResult
import io.rg.mp.utils.Preferences
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions.hasPermissions


class AuthViewModel(private val context: Context,
                    private val credential: GoogleAccountCredential,
                    private val preferences: Preferences,
                    private val spreadsheetService: SpreadsheetService) {
    companion object {
        const val REQUEST_ACCOUNT_PICKER = 1000
        const val REQUEST_AUTHORIZATION = 1001
        const val REQUEST_GOOGLE_PLAY_SERVICES = 1002
        const val REQUEST_PERMISSION_GET_ACCOUNTS = 1003
    }

    private val viewModelSubject = PublishSubject.create<ViewModelResult>()

    fun viewModelResultNotifier() = viewModelSubject.toFlowable(BackpressureStrategy.BUFFER)

    fun beginButtonClick() {
        authorize()
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_GOOGLE_PLAY_SERVICES -> if (resultCode != Activity.RESULT_OK) {
                viewModelSubject.onNext(
                        ToastInfo(R.string.requre_google_play_services, LENGTH_LONG)
                )
            } else {
                authorize()
            }

            REQUEST_ACCOUNT_PICKER -> if (resultCode == Activity.RESULT_OK && data != null &&
                    data.extras != null) {
                val accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                if (accountName != null) {
                    preferences.accountName = accountName
                    credential.selectedAccountName = accountName
                    authorize()
                }
            }

            REQUEST_AUTHORIZATION -> if (resultCode == Activity.RESULT_OK) {
                authorize()
            }
        }
    }

    private fun authorize() {
        if (credential.selectedAccountName == null) {
            chooseAccount()
        } else if (!isDeviceOnline()) {
            viewModelSubject.onNext(ToastInfo(R.string.no_network_message, LENGTH_SHORT))
        } else {
            downloadSpreadsheets()
        }
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private fun chooseAccount() {
        if (hasPermissions(context, Manifest.permission.GET_ACCOUNTS)) {
            val accountName = preferences.accountName
            if (accountName.isNotEmpty()) {
                credential.selectedAccountName = accountName
                authorize()
            } else {
                viewModelSubject.onNext(
                        StartActivity(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER)
                )
            }
        } else {
            viewModelSubject.onNext(
                    PermissionRequest(
                            arrayOf(GET_ACCOUNTS),
                            REQUEST_PERMISSION_GET_ACCOUNTS
                    )
            )
        }
    }

    private fun downloadSpreadsheets() {
        spreadsheetService.list()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        {
                            viewModelSubject.onComplete() },
                        {
                            handleErrors(it)
                        }
                )
    }

    private fun handleErrors(error: Throwable) {
        when (error) {
            is GooglePlayServicesAvailabilityIOException ->
                viewModelSubject.onNext(
                        GooglePlayServicesAvailabilityError(error.connectionStatusCode)
                )
            is UserRecoverableAuthIOException ->
                viewModelSubject.onNext(StartActivity(error.intent, REQUEST_AUTHORIZATION))
            else -> {
                viewModelSubject.onNext(ToastInfo(R.string.unknown_error, LENGTH_LONG))
            }
        }
    }

    private fun isDeviceOnline(): Boolean {
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
}