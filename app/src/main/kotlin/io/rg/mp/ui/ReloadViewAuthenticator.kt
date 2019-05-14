package io.rg.mp.ui

class ReloadViewAuthenticator {
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
}