package io.rg.mp.utils

import android.content.Context
import android.content.SharedPreferences


class Preferences(val context: Context) {
    companion object {
        private const val PREF_FILE_NAME = "io.rg.mp.shared"
        private const val PREF_ACCOUNT_NAME = "accountName"
        private const val PREF_CURRENT_SPREADSHEET_ID = "spreadsheet"
    }

    var accountName: String
        get() = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
                .getString(PREF_ACCOUNT_NAME, "")
        set(value) = save { it.putString(PREF_ACCOUNT_NAME, value) }

    val isAccountNameAvailable: Boolean
        get() = accountName.isNotEmpty()

    var spreadsheetId: String
        get() = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
                .getString(PREF_CURRENT_SPREADSHEET_ID, "")
        set(value) = save { it.putString(PREF_CURRENT_SPREADSHEET_ID, value) }

    val isSpreadsheetIdAvailable: Boolean
        get() = spreadsheetId.isNotEmpty()

    private fun save(putValue: (editor: SharedPreferences.Editor) -> Unit) {
        val settings = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
        val editor = settings.edit()
        putValue(editor)
        editor.apply()
    }
}