package io.rg.mp.ui.auth

import android.Manifest
import android.Manifest.permission.GET_ACCOUNTS
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast.LENGTH_LONG
import android.widget.Toast.LENGTH_SHORT
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.ReplaySubject
import io.rg.mp.R
import io.rg.mp.drive.SpreadsheetService
import io.rg.mp.ui.DisposableViewModel
import io.rg.mp.ui.model.GooglePlayServicesAvailabilityError
import io.rg.mp.ui.model.PermissionRequest
import io.rg.mp.ui.model.StartActivity
import io.rg.mp.ui.model.ToastInfo
import io.rg.mp.ui.model.ViewModelResult
import io.rg.mp.utils.GoogleApiAvailabilityService
import io.rg.mp.utils.Preferences
import io.rg.mp.utils.isDeviceOnline
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions.hasPermissions


class AuthViewModel(private val context: Context,
                    private val apiAvailabilityService: GoogleApiAvailabilityService,
                    private val credential: GoogleAccountCredential,
                    private val preferences: Preferences,
                    private val spreadsheetService: SpreadsheetService): DisposableViewModel {
    companion object {
        private const val TAG = "AuthViewModel"
        const val REQUEST_ACCOUNT_PICKER = 1000
        const val REQUEST_AUTHORIZATION = 1001
        const val REQUEST_GOOGLE_PLAY_SERVICES = 1002
        const val REQUEST_PERMISSION_GET_ACCOUNTS = 1003
    }

    private val viewModelSubject = ReplaySubject.create<ViewModelResult>()
    private val viewModelResultFlowable = viewModelSubject.toFlowable(BackpressureStrategy.BUFFER)
    private val compositeDisposable = CompositeDisposable()

    override fun clear() {
        compositeDisposable.dispose()
    }

    fun viewModelResultNotifier(): Flowable<ViewModelResult> = viewModelResultFlowable

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
        if (!apiAvailabilityService.isAvailable()) {
            apiAvailabilityService.acquire {
                viewModelSubject.onNext(GooglePlayServicesAvailabilityError(it))
            }
        } else if (credential.selectedAccountName == null) {
            chooseAccount()
        } else if (!context.isDeviceOnline()) {
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
        val disposable = spreadsheetService.list()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        {
                            viewModelSubject.onComplete()
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
                viewModelSubject.onNext(
                        GooglePlayServicesAvailabilityError(error.connectionStatusCode)
                )
            is UserRecoverableAuthIOException ->
                viewModelSubject.onNext(StartActivity(error.intent, REQUEST_AUTHORIZATION))
            else -> {
                Log.e(TAG, error.message, error)
                viewModelSubject.onNext(ToastInfo(R.string.unknown_error, LENGTH_LONG))
            }
        }
    }
}