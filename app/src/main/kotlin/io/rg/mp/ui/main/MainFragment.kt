package io.rg.mp.ui.main

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
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.Spinner
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.rg.mp.R
import io.rg.mp.persistence.dao.CategoryDao
import io.rg.mp.persistence.dao.SpreadsheetDao
import io.rg.mp.service.drive.SpreadsheetService
import io.rg.mp.service.sheet.CategoryRetrieverService
import io.rg.mp.ui.main.adapter.CategorySpinnerAdapter
import io.rg.mp.ui.main.adapter.SpreadsheetSpinnerAdapter
import io.rg.mp.utils.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject


class MainFragment : Fragment() {
    companion object {
        private const val REQUEST_ACCOUNT_PICKER = 1000
        private const val REQUEST_AUTHORIZATION = 1001
        private const val REQUEST_GOOGLE_PLAY_SERVICES = 1002
        private const val REQUEST_PERMISSION_GET_ACCOUNTS = 1003
    }


    @Inject lateinit var categoryService: CategoryRetrieverService
    @Inject lateinit var spreadsheetService: SpreadsheetService
    @Inject lateinit var categoryDao: CategoryDao
    @Inject lateinit var spreadsheetDao: SpreadsheetDao
    @Inject lateinit var toasts: Toasts
    @Inject lateinit var preferences: Preferences
    @Inject lateinit var credential: GoogleAccountCredential

    lateinit var categorySpinner: Spinner
    lateinit var categorySpinnerAdapter: CategorySpinnerAdapter

    lateinit var spreadsheetSpinner: Spinner
    lateinit var spreadsheetSpinnerAdapter: SpreadsheetSpinnerAdapter

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_main, container, false)

        categorySpinner = view.findViewById(R.id.category_spinner)
        categorySpinnerAdapter = CategorySpinnerAdapter(
                activity, android.R.layout.simple_spinner_dropdown_item, activity.layoutInflater)
        categorySpinner.adapter = categorySpinnerAdapter

        spreadsheetSpinner = view.findViewById(R.id.spreadsheet_spinner)
        spreadsheetSpinnerAdapter = SpreadsheetSpinnerAdapter(
                activity, android.R.layout.simple_spinner_dropdown_item, activity.layoutInflater)
        spreadsheetSpinner.adapter = spreadsheetSpinnerAdapter
        spreadsheetSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, v: View, pos: Int, id: Long) {
                val spreadsheet = spreadsheetSpinnerAdapter.getItem(pos)
                preferences.spreadsheetId = spreadsheet.id
                reloadCategories()
                downloadCategories()
            }

            override fun onNothingSelected(parent: AdapterView<out Adapter>?) {}
        }

        return view
    }

    override fun onStart() {
        super.onStart()

        getResultsFromApi()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onActivityResult(
            requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_GOOGLE_PLAY_SERVICES -> if (resultCode != Activity.RESULT_OK) {
                toasts.requireGooglePlayServices(activity)
            } else {
                getResultsFromApi()
            }

            REQUEST_ACCOUNT_PICKER -> if (resultCode == Activity.RESULT_OK && data != null &&
                    data.extras != null) {
                val accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                if (accountName != null) {
                    preferences.accountName = accountName
                    credential.selectedAccountName = accountName
                    getResultsFromApi()
                }
            }

            REQUEST_AUTHORIZATION -> if (resultCode == Activity.RESULT_OK) {
                getResultsFromApi()
            }
        }
    }

    fun getResultsFromApi() {
        if (!activity.isGooglePlayServicesAvailable()) {
            activity.acquireGooglePlayServices(this::showGooglePlayServicesAvailabilityErrorDialog)
        } else if (credential.selectedAccountName == null) {
            chooseAccount()
        } else if (!activity.isDeviceOnline()) {
            toasts.noNetwork(activity)
        } else {
            reloadSpreadsheets()
            reloadCategories()

            downloadSpreadsheets()
            if (preferences.isSpreadsheetIdAvailable) {
                downloadCategories()
            }
        }
    }

    fun showGooglePlayServicesAvailabilityErrorDialog(
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
                getResultsFromApi()
            } else {
                startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER)
            }
        } else {
            EasyPermissions.requestPermissions(
                    activity,
                    activity.getString(R.string.access_to_contacts),
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS)
        }
    }

    private fun reloadCategories() {
        categoryDao.findBySpreadsheetId(preferences.spreadsheetId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    categorySpinnerAdapter.setItems(it)
                }
    }

    private fun reloadSpreadsheets() {
        spreadsheetDao.all()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    spreadsheetSpinnerAdapter.setItems(it)

                    val position = it.indexOfFirst{ (id) -> id == preferences.spreadsheetId }
                    spreadsheetSpinner.setSelection(position)
                }
    }

    private fun downloadSpreadsheets() {
        spreadsheetService.list()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(
                        {
                            (list) -> spreadsheetDao.insertAll(*list.toTypedArray())
                        },
                        { error ->
                            if (error is UserRecoverableAuthIOException) {
                                startActivityForResult(
                                        error.intent,
                                        REQUEST_AUTHORIZATION)
                            }
                        },
                        {}
                )
    }

    private fun downloadCategories() {
        val spreadsheetId = preferences.spreadsheetId

        categoryService.all(spreadsheetId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe { (list) -> categoryDao.insertAll(*list.toTypedArray()) }
    }
}