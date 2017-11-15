package io.rg.mp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.junit.BeforeClass
import java.lang.reflect.Field
import java.lang.reflect.Modifier

abstract class AndroidContextAwareTest {
    protected val context: Context = mock()

    companion object {
        @BeforeClass
        @JvmStatic
        fun initializeAndroidContextAwareTest() {
            val sdkIntField = Build.VERSION::class.java.getDeclaredField("SDK_INT")
            val modifiersField = Field::class.java.getDeclaredField("modifiers")
            modifiersField.isAccessible = true
            modifiersField.setInt(sdkIntField, sdkIntField.modifiers and Modifier.FINAL.inv())
            sdkIntField.set(Build.VERSION.SDK_INT, 25)
        }
    }

    protected fun hasNoPermission() {
        setPermissionResult(PackageManager.PERMISSION_DENIED)
    }

    protected fun hasPermission() {
        setPermissionResult(PackageManager.PERMISSION_GRANTED)
    }

    private fun setPermissionResult(permissionResult: Int) {
        whenever(context.checkPermission(eq(Manifest.permission.GET_ACCOUNTS), any(), any()))
                .thenReturn(permissionResult)
    }

    protected fun deviceOnline() {
        deviceOnline(true)
    }

    protected fun deviceOffline() {
        deviceOnline(false)
    }

    private fun deviceOnline(isOnline: Boolean) {
        val connectionManager: ConnectivityManager = mock()
        val networkInfo: NetworkInfo = mock()

        whenever(context.getSystemService(eq(Context.CONNECTIVITY_SERVICE)))
                .thenReturn(connectionManager)
        whenever(connectionManager.activeNetworkInfo).thenReturn(networkInfo)
        whenever(networkInfo.isConnected).thenReturn(isOnline)
    }
}