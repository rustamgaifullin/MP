package io.rg.mp.ui.auth

import android.Manifest
import android.Manifest.permission.GET_ACCOUNTS
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast.LENGTH_LONG
import android.widget.Toast.LENGTH_SHORT
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.rg.mp.R
import io.rg.mp.drive.SpreadsheetService
import io.rg.mp.ui.AbstractViewModel
import io.rg.mp.ui.GooglePlayServicesAvailabilityError
import io.rg.mp.ui.PermissionRequest
import io.rg.mp.ui.StartActivity
import io.rg.mp.ui.ToastInfo
import io.rg.mp.utils.GoogleApiAvailabilityService
import io.rg.mp.utils.Preferences
import io.rg.mp.utils.isDeviceOnline
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions.hasPermissions


class AuthViewModel(private val context: Context,
                    private val apiAvailabilityService: GoogleApiAvailabilityService,
                    private val credential: GoogleAccountCredential,
                    private val preferences: Preferences,
                    private val spreadsheetService: SpreadsheetService): AbstractViewModel() {
    companion object {
        const val REQUEST_ACCOUNT_PICKER = 1000
        const val REQUEST_AUTHORIZATION = 1001
        const val REQUEST_GOOGLE_PLAY_SERVICES = 1002
        const val REQUEST_PERMISSION_GET_ACCOUNTS = 1003
    }

    fun beginButtonClick() {
        authorize()
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_GOOGLE_PLAY_SERVICES -> if (resultCode != Activity.RESULT_OK) {
                subject.onNext(
                        ToastInfo(R.string.require_google_play_services, LENGTH_LONG)
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
        if (!apiAvailabilityService.isAvailable()) {
            apiAvailabilityService.acquire {
                subject.onNext(GooglePlayServicesAvailabilityError(it))
            }
        } else if (credential.selectedAccountName == null) {
            chooseAccount()
        } else if (!context.isDeviceOnline()) {
            subject.onNext(ToastInfo(R.string.no_network_message, LENGTH_SHORT))
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
                subject.onNext(
                        StartActivity(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER)
                )
            }
        } else {
            subject.onNext(
                    PermissionRequest(
                            arrayOf(GET_ACCOUNTS),
                            REQUEST_PERMISSION_GET_ACCOUNTS
                    )
            )
        }
    }

    private fun downloadSpreadsheets() {
        val disposable = spreadsheetService.list()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        {
                            subject.onComplete()
                        },
                        {
                            handleErrors(it)
                        }
                )
        compositeDisposable.add(disposable)
    }

    private fun handleErrors(error: Throwable) {
        when (error) {
            is GooglePlayServicesAvailabilityIOException ->
                subject.onNext(
                        GooglePlayServicesAvailabilityError(error.connectionStatusCode)
                )
            is UserRecoverableAuthIOException ->
                subject.onNext(StartActivity(error.intent, REQUEST_AUTHORIZATION))
            else -> {
                subject.onNext(ToastInfo(R.string.unknown_error, LENGTH_LONG))
            }
        }
    }
}