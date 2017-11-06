package io.rg.mp.service

import android.content.Context
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.drive.Drive
import com.google.api.services.sheets.v4.Sheets
import dagger.Module
import dagger.Provides
import io.rg.mp.service.config.ApplicationName
import io.rg.mp.service.config.Scopes
import io.rg.mp.utils.Preferences
import javax.inject.Singleton

@Module
class ServiceModule {
    @Provides
    @Singleton
    fun transport(): HttpTransport = AndroidHttp.newCompatibleTransport()

    @Provides
    @Singleton
    fun jsonFactory(): JacksonFactory = JacksonFactory.getDefaultInstance()

    @Provides
    @Singleton
    fun scopes() = Scopes()

    @Provides
    @Singleton
    fun credential(
            context: Context,
            scopes: Scopes,
            preferences: Preferences): GoogleAccountCredential {
        return GoogleAccountCredential.usingOAuth2(context, scopes.list)
                .setBackOff(ExponentialBackOff())
                .setSelectedAccountName(preferences.accountName)
    }

    @Provides
    @Singleton
    fun createSheetsService(
            credential: GoogleAccountCredential,
            httpTransport: HttpTransport,
            jsonFactory: JacksonFactory): Sheets {
        return Sheets.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName(ApplicationName.SHEET)
                .build()
    }

    @Provides
    @Singleton
    fun createDriveService(
            credential: GoogleAccountCredential,
            httpTransport: HttpTransport,
            jsonFactory: JacksonFactory): Drive {
        return Drive.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName(ApplicationName.DRIVE)
                .build()
    }
}