package io.rg.mp.service

import com.google.api.services.drive.DriveScopes
import com.google.api.services.sheets.v4.SheetsScopes

class Scopes {
    val list = listOf(SheetsScopes.SPREADSHEETS_READONLY, DriveScopes.DRIVE_READONLY)
}