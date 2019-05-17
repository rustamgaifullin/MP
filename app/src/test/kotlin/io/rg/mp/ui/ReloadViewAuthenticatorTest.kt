package io.rg.mp.ui

import android.os.Bundle
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import io.rg.mp.ui.ReloadViewAuthenticator.Companion.IS_NOT_IN_AUTHENTICATION
import org.junit.Test

class ReloadViewAuthenticatorTest {
    @Test
    fun `should invoke reload action`() {
        val sut = ReloadViewAuthenticator()
        val callback: () -> Unit = mock()
        sut.startReload(callback)

        verify(callback, times(1)).invoke()
    }

    @Test
    fun `should not invoke reload action when authentication is not finished`() {
        val sut = ReloadViewAuthenticator()
        sut.startAuthentication {  }
        val callback: () -> Unit = mock()
        sut.startReload(callback)

        verify(callback, never()).invoke()
    }

    @Test
    fun `should invoke reload action when authentication started and finished`() {
        val sut = ReloadViewAuthenticator()
        sut.startAuthentication {  }
        sut.authenticationFinished()
        val callback: () -> Unit = mock()
        sut.startReload(callback)

        verify(callback, times(1)).invoke()
    }

    @Test
    fun `should invoke authentication only once`() {
        val sut = ReloadViewAuthenticator()
        val callback: () -> Unit = mock()
        sut.startAuthentication(callback)
        sut.startAuthentication(callback)
        sut.startAuthentication(callback)

        verify(callback, times(1)).invoke()
    }

    @Test
    fun `should not invoke reload action when authentication is not finished after state restore`() {
        val sut = ReloadViewAuthenticator()
        val bundle = Bundle()
        bundle.putBoolean(IS_NOT_IN_AUTHENTICATION, false)
        sut.restoreState(bundle)
        val callback: () -> Unit = mock()
        sut.startReload(callback)

        verify(callback, never()).invoke()
    }

}