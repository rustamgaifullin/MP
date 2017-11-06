package io.rg.mp.service.config

import com.google.api.services.drive.DriveScopes
import com.google.api.services.sheets.v4.SheetsScopes

class Scopes {
    val list = listOf(
            SheetsScopes.SPREADSHEETS,
            DriveScopes.DRIVE_READONLY
    )
}