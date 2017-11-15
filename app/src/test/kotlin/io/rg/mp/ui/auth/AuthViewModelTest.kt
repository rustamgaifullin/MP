package io.rg.mp.ui.auth

import android.Manifest.permission.GET_ACCOUNTS
import android.accounts.AccountManager.KEY_ACCOUNT_NAME
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Flowable
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.TestSubscriber
import io.rg.mp.AndroidContextAwareTest
import io.rg.mp.R
import io.rg.mp.service.drive.SpreadsheetList
import io.rg.mp.service.drive.SpreadsheetService
import io.rg.mp.ui.auth.AuthViewModel.Companion.REQUEST_ACCOUNT_PICKER
import io.rg.mp.ui.auth.AuthViewModel.Companion.REQUEST_AUTHORIZATION
import io.rg.mp.ui.auth.AuthViewModel.Companion.REQUEST_GOOGLE_PLAY_SERVICES
import io.rg.mp.ui.auth.AuthViewModel.Companion.REQUEST_PERMISSION_GET_ACCOUNTS
import io.rg.mp.ui.model.GooglePlayServicesAvailabilityError
import io.rg.mp.ui.model.PermissionRequest
import io.rg.mp.ui.model.StartActivity
import io.rg.mp.ui.model.ToastInfo
import io.rg.mp.ui.model.ViewModelResult
import io.rg.mp.utils.Preferences
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.mockito.Mockito


class AuthViewModelTest : AndroidContextAwareTest() {
    private val credential: GoogleAccountCredential = mock()
    private val preferences: Preferences = mock()
    private val spreadsheetService: SpreadsheetService = mock()
    private lateinit var testSubscriber: TestSubscriber<ViewModelResult>

    companion object {
        @JvmStatic
        @BeforeClass
        fun setupRxJava() {
            RxJavaPlugins.setIoSchedulerHandler {
                Schedulers.trampoline()
            }
            RxAndroidPlugins.setMainThreadSchedulerHandler {
                Schedulers.trampoline()
            }
        }

        @JvmStatic
        @AfterClass
        fun cleanUpRxJava() {
            RxJavaPlugins.reset()
            RxAndroidPlugins.reset()
        }

    }

    @Before
    fun setup() {
        testSubscriber = TestSubscriber()
    }

    @Test
    fun `should request get_account permission`() {
        val sut = authViewModel()

        whenever(credential.selectedAccountName).thenReturn(null)
        hasNoPermission()
        sut.viewModelResultNotifier().subscribe(testSubscriber)
        sut.beginButtonClick()

        testSubscriber
                .assertNoErrors()
                .assertValue {
                    it is PermissionRequest &&
                            it.permissions[0] == GET_ACCOUNTS &&
                            it.requestCode == REQUEST_PERMISSION_GET_ACCOUNTS
                }
    }

    @Test
    fun `should start account picker activity`() {
        val sut = authViewModel()
        val intent: Intent = mock()

        hasPermission()
        whenever(preferences.accountName).thenReturn("")
        whenever(credential.newChooseAccountIntent()).thenReturn(intent)
        sut.viewModelResultNotifier().subscribe(testSubscriber)
        sut.beginButtonClick()

        testSubscriber
                .assertNoErrors()
                .assertValue {
                    it is StartActivity && it.requestCode == AuthViewModel.REQUEST_ACCOUNT_PICKER
                }
    }

    @Test
    fun `should successfully authenticate and complete when account doesn't set`() {
        val sut = authViewModel()
        val accountName = "asdf@asdf.as"

        hasPermission()
        deviceOnline()
        whenever(preferences.accountName).thenReturn(accountName)
        whenever(credential.selectedAccountName)
                .thenReturn(null)
                .thenReturn(accountName)

        whenever(spreadsheetService.list()).thenReturn(Flowable.just(SpreadsheetList(emptyList())))
        sut.viewModelResultNotifier().subscribe(testSubscriber)
        sut.beginButtonClick()

        verify(credential).selectedAccountName = eq(accountName)
        testSubscriber
                .assertNoErrors()
                .assertNoValues()
                .assertComplete()
    }

    @Test
    fun `should successfully authenticate and complete when account set`() {
        val sut = authViewModel()
        val accountName = "asdf@asdf.as"

        hasPermission()
        deviceOnline()
        whenever(credential.selectedAccountName).thenReturn(accountName)
        whenever(spreadsheetService.list()).thenReturn(Flowable.just(SpreadsheetList(emptyList())))
        sut.viewModelResultNotifier().subscribe(testSubscriber)
        sut.beginButtonClick()

        testSubscriber
                .assertNoErrors()
                .assertNoValues()
                .assertComplete()
    }

    @Test
    fun `should react properly after checking google play services`() {
        val sut = authViewModel()

        hasPermission()
        deviceOnline()
        whenever(credential.selectedAccountName).thenReturn("asdf@asdf.as")
        whenever(spreadsheetService.list()).thenReturn(Flowable.empty())
        sut.viewModelResultNotifier().subscribe(testSubscriber)
        sut.onActivityResult(REQUEST_GOOGLE_PLAY_SERVICES, 123, Intent())

        testSubscriber
                .assertNoErrors()
                .assertValueCount(1)
                .assertValueAt(0, {
                    it is ToastInfo && it.messageId == R.string.requre_google_play_services
                })
        verifyZeroInteractions(spreadsheetService)

        sut.onActivityResult(REQUEST_GOOGLE_PLAY_SERVICES, RESULT_OK, Intent())

        testSubscriber
                .assertNoErrors()
                .assertValueCount(1)
        verify(spreadsheetService).list()
    }

    @Test
    fun `should authorize only when account was picked`() {
        val sut = authViewModel()
        val accountName = "asdf@asdf.as"
        val intent: Intent = mock()
        val bundle: Bundle = mock()

        hasPermission()
        deviceOnline()
        whenever(credential.selectedAccountName).thenReturn(accountName)
        whenever(spreadsheetService.list()).thenReturn(Flowable.empty())
        whenever(intent.extras)
                .thenReturn(bundle)
                .thenReturn(null)
        whenever(intent.getStringExtra(eq(KEY_ACCOUNT_NAME)))
                .thenReturn(accountName)

        sut.onActivityResult(REQUEST_ACCOUNT_PICKER, RESULT_OK, intent)
        verify(spreadsheetService).list()

        sut.onActivityResult(REQUEST_ACCOUNT_PICKER, RESULT_CANCELED, intent)
        verifyZeroInteractions(spreadsheetService)

        sut.onActivityResult(REQUEST_ACCOUNT_PICKER, RESULT_OK, null)
        verifyZeroInteractions(spreadsheetService)

        sut.onActivityResult(REQUEST_ACCOUNT_PICKER, RESULT_OK, intent)
        verifyZeroInteractions(spreadsheetService)
    }

    @Test
    fun `should authorize if user accepted requested permissions`() {
        val sut = authViewModel()

        hasPermission()
        deviceOnline()
        whenever(credential.selectedAccountName).thenReturn("")
        whenever(spreadsheetService.list()).thenReturn(Flowable.empty())

        sut.onActivityResult(REQUEST_AUTHORIZATION, RESULT_OK, Intent())
        verify(spreadsheetService).list()

        sut.onActivityResult(REQUEST_AUTHORIZATION, RESULT_CANCELED, Intent())
        verifyZeroInteractions(spreadsheetService)
    }

    @Test
    fun `should show toast when internet is not available`() {
        val sut = authViewModel()

        deviceOffline()
        whenever(credential.selectedAccountName).thenReturn("")
        sut.viewModelResultNotifier().subscribe(testSubscriber)
        sut.beginButtonClick()

        testSubscriber
                .assertNoErrors()
                .assertValue {
                    it is ToastInfo && it.messageId == R.string.no_network_message
                }
    }

    @Test
    fun `should start activity when authentication error is occurred`() {
        val sut = authViewModel()

        hasPermission()
        deviceOnline()
        whenever(credential.selectedAccountName).thenReturn("")
        whenever(spreadsheetService.list())
                .thenReturn(Flowable.error(
                        userRecoverableAuthIoException())
                )
        sut.viewModelResultNotifier().subscribe(testSubscriber)
        sut.beginButtonClick()

        testSubscriber
                .assertValue { it is StartActivity && it.requestCode == REQUEST_AUTHORIZATION }
                .assertNoErrors()
                .assertNotComplete()
    }

    @Test
    fun `should show dialog when google play services is not available`() {
        val sut = authViewModel()

        hasPermission()
        deviceOnline()
        whenever(credential.selectedAccountName).thenReturn("")
        whenever(spreadsheetService.list())
                .thenReturn(Flowable.error(
                        googlePlayServiceAvailabilityError()
                ))
        sut.viewModelResultNotifier().subscribe(testSubscriber)
        sut.beginButtonClick()

        testSubscriber
                .assertNoErrors()
                .assertValue { it is GooglePlayServicesAvailabilityError }
                .assertNotComplete()
    }

    @Test
    fun `should show toast in case of unexpected exceptions`() {
        val sut = authViewModel()

        hasPermission()
        deviceOnline()
        whenever(credential.selectedAccountName).thenReturn("")
        whenever(spreadsheetService.list())
                .thenReturn(Flowable.error(
                        Exception()
                ))
        sut.viewModelResultNotifier().subscribe(testSubscriber)
        sut.beginButtonClick()

        testSubscriber
                .assertNoErrors()
                .assertValue { it is ToastInfo && it.messageId == R.string.unknown_error }
                .assertNotComplete()
    }

    private fun authViewModel() = AuthViewModel(context, credential, preferences, spreadsheetService)

    private fun userRecoverableAuthIoException(): UserRecoverableAuthIOException {
        val wrapper = UserRecoverableAuthException("", Intent())
        return UserRecoverableAuthIOException(wrapper)
    }

    private fun googlePlayServiceAvailabilityError(): GooglePlayServicesAvailabilityIOException {
        val mockedWrapper = Mockito.mock(GooglePlayServicesAvailabilityException::class.java)
        return GooglePlayServicesAvailabilityIOException(mockedWrapper)
    }
}

