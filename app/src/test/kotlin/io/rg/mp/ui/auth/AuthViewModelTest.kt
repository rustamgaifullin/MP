package io.rg.mp.ui.auth

import android.Manifest
import android.Manifest.permission.GET_ACCOUNTS
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Flowable
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.TestSubscriber
import io.rg.mp.service.drive.SpreadsheetList
import io.rg.mp.service.drive.SpreadsheetService
import io.rg.mp.ui.auth.AuthViewModel.Companion.REQUEST_PERMISSION_GET_ACCOUNTS
import io.rg.mp.utils.Preferences
import org.junit.BeforeClass
import org.junit.Test
import java.lang.reflect.Field
import java.lang.reflect.Modifier




class AuthViewModelTest {
    val context: Context = mock()
    val credential: GoogleAccountCredential = mock()
    val preferences: Preferences = mock()
    val spreadsheetService: SpreadsheetService = mock()

    companion object {
        @BeforeClass
        @JvmStatic
        fun setup() {
            val sdkIntField = Build.VERSION::class.java.getDeclaredField("SDK_INT")
            val modifiersField = Field::class.java.getDeclaredField("modifiers")
            modifiersField.isAccessible = true
            modifiersField.setInt(sdkIntField, sdkIntField.modifiers and Modifier.FINAL.inv())
            sdkIntField.set(Build.VERSION.SDK_INT, 25)

            RxJavaPlugins.setInitIoSchedulerHandler {
                Schedulers.single()
            }
            RxAndroidPlugins.setInitMainThreadSchedulerHandler {
                Schedulers.single()
            }
        }
    }

    fun hasNoPermission() {
        getPermission(PackageManager.PERMISSION_DENIED)
    }

    fun hasPermission() {
        getPermission(PackageManager.PERMISSION_GRANTED)
    }

    fun getPermission(permissionResult: Int) {
        whenever(context.checkPermission(eq(Manifest.permission.GET_ACCOUNTS), any(), any()))
                .thenReturn(permissionResult)
    }

    fun deviceOnline() {
        deviceOnline(true)
    }

    fun deviceOffline() {
        deviceOnline(false)
    }

    fun deviceOnline(isOnline: Boolean) {
        val connectionManager: ConnectivityManager = mock()
        val networkInfo: NetworkInfo = mock()

        whenever(context.getSystemService(eq(Context.CONNECTIVITY_SERVICE)))
                .thenReturn(connectionManager)
        whenever(connectionManager.activeNetworkInfo).thenReturn(networkInfo)
        whenever(networkInfo.isConnected).thenReturn(isOnline)
    }


    fun authViewModel() = AuthViewModel(context, credential, preferences, spreadsheetService)

    @Test
    fun `should request get_account permission`() {
        val sut = authViewModel()
        val testSubscriber = TestSubscriber<PermissionRequest>()

        whenever(credential.selectedAccountName).thenReturn(null)
        hasNoPermission()
        sut.requestPermission().subscribe(testSubscriber)
        sut.beginButtonClick()

        testSubscriber.assertNoErrors()
        testSubscriber.assertValue { (permissions, requestCode) ->
            permissions[0] == GET_ACCOUNTS && requestCode == REQUEST_PERMISSION_GET_ACCOUNTS
        }
    }

    @Test
    fun `should start account picker activity`() {
            val sut = authViewModel()
            val testSubscriber = TestSubscriber<ActivityToStart>()
            val intent: Intent = mock()

            hasPermission()
            whenever(preferences.accountName).thenReturn("")
            whenever(credential.newChooseAccountIntent()).thenReturn(intent)
            sut.startActivity().subscribe(testSubscriber)
            sut.beginButtonClick()

            testSubscriber.assertNoErrors()
            testSubscriber.assertValue { (_, requestCode) ->
                requestCode == AuthViewModel.REQUEST_ACCOUNT_PICKER
            }
    }

    @Test
    fun `should successfully authenticate and complete when account doesn't set`() {
        val sut = authViewModel()
        val accountName = "asdf@asdf.as"
        val testSubscriber = TestSubscriber<Any>()

        hasPermission()
        deviceOnline()
        whenever(preferences.accountName).thenReturn(accountName)
        whenever(credential.selectedAccountName)
                .thenReturn(null)
                .thenReturn(accountName)

        whenever(spreadsheetService.list()).thenReturn(Flowable.just(SpreadsheetList(emptyList())))
        sut.allDone().subscribe(testSubscriber)
        sut.beginButtonClick()

        testSubscriber.awaitTerminalEvent()

        verify(credential).selectedAccountName = eq(accountName)
        testSubscriber.assertNoErrors()
        testSubscriber.assertNoValues()
        testSubscriber.assertComplete()
    }

    @Test
    fun `should successfully authenticate and complete when account set`() {
        val sut = authViewModel()
        val accountName = "asdf@asdf.as"
        val testSubscriber = TestSubscriber<Any>()

        hasPermission()
        deviceOnline()
        whenever(credential.selectedAccountName).thenReturn(accountName)
        whenever(spreadsheetService.list()).thenReturn(Flowable.just(SpreadsheetList(emptyList())))
        sut.allDone().subscribe(testSubscriber)
        sut.beginButtonClick()

        testSubscriber.awaitTerminalEvent()

        testSubscriber.assertNoErrors()
        testSubscriber.assertNoValues()
        testSubscriber.assertComplete()

    }
}

