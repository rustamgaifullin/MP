package io.rg.mp.ui

import android.os.Bundle

class ReloadViewAuthenticator {
    companion object {
        const val IS_NOT_IN_AUTHENTICATION = "io.rg.mp.IsNotInAuthentication"
    }

    private var isNotInAuthentication = true

    fun startReload(reloadAction: () -> Unit) {
        if (isNotInAuthentication) {
            reloadAction.invoke()
        }
    }

    fun startAuthentication(authAction: () -> Unit) {
        if (isNotInAuthentication) {
            isNotInAuthentication = false
            authAction.invoke()
        }
    }

    fun authenticationFinished() {
        isNotInAuthentication = true
    }

    fun getState(): Bundle {
        val bundle = Bundle()

        bundle.putBoolean(IS_NOT_IN_AUTHENTICATION, isNotInAuthentication)

        return bundle
    }

    fun restoreState(bundle: Bundle?) {
        isNotInAuthentication = bundle?.getBoolean(IS_NOT_IN_AUTHENTICATION) ?: true
    }
}