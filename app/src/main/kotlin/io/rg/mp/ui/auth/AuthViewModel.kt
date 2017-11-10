package io.rg.mp.ui.auth

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.widget.Toast
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.rg.mp.R
import io.rg.mp.service.drive.SpreadsheetService
import io.rg.mp.utils.Preferences
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions.hasPermissions


typealias RequestCode = Int
typealias ActivityToStart = Pair<Intent, RequestCode>
typealias Permissions = Array<String>
typealias PermissionRequest = Pair<Permissions, RequestCode>

typealias MessageId = Int
typealias Length = Int
typealias ToastInfo = Pair<MessageId, Length>

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

    private val startActivitySubject = PublishSubject.create<ActivityToStart>()
    private val requestPermissionSubject = PublishSubject.create<PermissionRequest>()
    private val allDoneSubject = PublishSubject.create<Any>()
    private val showToastSubject = PublishSubject.create<ToastInfo>()

    fun startActivity() = startActivitySubject.toFlowable(BackpressureStrategy.BUFFER)
    fun requestPermission() = requestPermissionSubject.toFlowable(BackpressureStrategy.BUFFER)
    fun allDone() = allDoneSubject.toFlowable(BackpressureStrategy.BUFFER)
    fun showToast() = showToastSubject.toFlowable(BackpressureStrategy.BUFFER)

    fun beginButtonClick() {
        authorize()
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_GOOGLE_PLAY_SERVICES -> if (resultCode != Activity.RESULT_OK) {
                showToastSubject.onNext(
                        ToastInfo(R.string.requre_google_play_services, Toast.LENGTH_LONG))
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
            showToastSubject.onNext(ToastInfo(R.string.no_network_message, Toast.LENGTH_SHORT))
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
                val activityInfo = ActivityToStart(
                        credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER)
                startActivitySubject.onNext(activityInfo)
            }
        } else {
            val permissionInfo = PermissionRequest(
                    arrayOf(Manifest.permission.GET_ACCOUNTS), REQUEST_PERMISSION_GET_ACCOUNTS)
            requestPermissionSubject.onNext(permissionInfo)
        }
    }

    private fun downloadSpreadsheets() {
        spreadsheetService.list()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { finish() },
                        { handleErrors(it) }
                )
    }

    private fun finish() {
        allDoneSubject.onComplete()
        startActivitySubject.onComplete()
        requestPermissionSubject.onComplete()
        showToastSubject.onComplete()
    }

    private fun handleErrors(error: Throwable) {
        when (error) {
            is UserRecoverableAuthIOException ->
                startActivitySubject.onNext(ActivityToStart(error.intent, REQUEST_AUTHORIZATION))
        }
    }

    private fun isDeviceOnline(): Boolean {
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
}