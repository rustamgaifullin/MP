package io.rg.mp.ui.auth


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import com.google.android.gms.common.GoogleApiAvailability
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.rg.mp.R
import io.rg.mp.ui.GooglePlayServicesAvailabilityError
import io.rg.mp.ui.PermissionRequest
import io.rg.mp.ui.StartActivity
import io.rg.mp.ui.ToastInfo
import io.rg.mp.ui.ViewModelResult
import io.rg.mp.ui.auth.AuthViewModel.Companion.REQUEST_GOOGLE_PLAY_SERVICES
import io.rg.mp.ui.expense.ExpenseFragment
import kotlinx.android.synthetic.main.fragment_auth.*
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject


class AuthFragment : Fragment() {
    companion object {
        const val NAME = "AUTH_FRAGMENT"
    }

    @Inject lateinit var authViewModel: AuthViewModel

    private val compositeDisposable = CompositeDisposable()

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_auth, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        beginButton.setOnClickListener { _ -> authViewModel.beginButtonClick() }
    }

    override fun onStart() {
        super.onStart()
        compositeDisposable.add(authViewModel.viewModelResultNotifier()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        handleNextResult(),
                        handleError(),
                        finish()
                )
        )
    }

    private fun handleNextResult(): (ViewModelResult) -> Unit {
        return {
            when (it) {
                is ToastInfo ->
                    Toast.makeText(activity, it.messageId, it.length).show()

                is StartActivity ->
                    startActivityForResult(it.intent, it.requestCode)

                is PermissionRequest ->
                    requestPermissions(it.permissions, it.requestCode)

                is GooglePlayServicesAvailabilityError ->
                    showGooglePlayServicesAvailabilityErrorDialog(it.requestCode)
            }
        }
    }

    private fun showGooglePlayServicesAvailabilityErrorDialog(
            connectionStatusCode: Int) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog = apiAvailability.getErrorDialog(
                activity,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES)
        dialog.show()
    }

    private fun handleError(): (Throwable) -> Unit {
        return {
            Toast.makeText(activity, it.message, LENGTH_LONG).show()
        }
    }

    private fun finish(): () -> Unit {
        return {
            fragmentManager?.apply {
                val transaction = beginTransaction()
                transaction.replace(R.id.main_container, ExpenseFragment())
                transaction.commit()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.clear()
        authViewModel.clear()
    }

    override fun onActivityResult(
            requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        authViewModel.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, authViewModel)
    }
}