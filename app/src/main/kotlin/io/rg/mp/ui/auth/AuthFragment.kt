package io.rg.mp.ui.auth


import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import dagger.android.support.AndroidSupportInjection
import io.rg.mp.R
import io.rg.mp.ui.expense.ExpenseFragment
import io.rg.mp.utils.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject


class AuthFragment : Fragment() {
    companion object {
        private const val REQUEST_ACCOUNT_PICKER = 1000
        private const val REQUEST_AUTHORIZATION = 1001
        private const val REQUEST_GOOGLE_PLAY_SERVICES = 1002
        private const val REQUEST_PERMISSION_GET_ACCOUNTS = 1003
    }

    @Inject lateinit var toasts: Toasts
    @Inject lateinit var preferences: Preferences
    @Inject lateinit var credential: GoogleAccountCredential

    private lateinit var beginButton: Button

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_auth, container, false)

        beginButton = view.findViewById(R.id.begin_button)
        beginButton.setOnClickListener { _ -> beginButtonClick() }

        return view
    }

    private fun beginButtonClick() {
        authorize()
    }

    private fun authorize() {
        if (!activity.isGooglePlayServicesAvailable()) {
            activity.acquireGooglePlayServices(this::showGooglePlayServicesAvailabilityErrorDialog)
        } else if (credential.selectedAccountName == null) {
            chooseAccount()
        } else if (!activity.isDeviceOnline()) {
            toasts.noNetwork(activity)
        } else {
            finish()
        }
    }

    override fun onActivityResult(
            requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_GOOGLE_PLAY_SERVICES -> if (resultCode != Activity.RESULT_OK) {
                toasts.requireGooglePlayServices(activity)
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

    private fun showGooglePlayServicesAvailabilityErrorDialog(
            activity: Activity,
            connectionStatusCode: Int) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog = apiAvailability.getErrorDialog(
                activity,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES)
        dialog.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private fun chooseAccount() {
        if (EasyPermissions.hasPermissions(activity, Manifest.permission.GET_ACCOUNTS)) {
            val accountName = preferences.accountName
            if (accountName.isNotEmpty()) {
                credential.selectedAccountName = accountName
                authorize()
            } else {
                startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER)
            }
        } else {
            requestPermissions(
                    arrayOf(Manifest.permission.GET_ACCOUNTS),
                    REQUEST_PERMISSION_GET_ACCOUNTS)
        }
    }

    private fun finish() {
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.main_container, ExpenseFragment())
        transaction.commit()
    }
}