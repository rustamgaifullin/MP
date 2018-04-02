package io.rg.mp

import com.google.api.client.googleapis.testing.auth.oauth2.MockGoogleCredential
import com.google.api.client.http.LowLevelHttpRequest
import com.google.api.client.http.LowLevelHttpResponse
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.testing.http.MockHttpTransport
import com.google.api.client.testing.http.MockLowLevelHttpRequest
import com.google.api.client.testing.http.MockLowLevelHttpResponse
import com.google.api.services.sheets.v4.Sheets
import java.io.IOException
import java.util.LinkedList

fun mockSheetClient(responses: LinkedList<MockLowLevelHttpResponse>): Sheets {
    val mockTransport = object : MockHttpTransport() {
        override fun buildRequest(method: String, url: String): LowLevelHttpRequest {
            return object : MockLowLevelHttpRequest() {
                @Throws(IOException::class)
                override fun execute(): LowLevelHttpResponse {
                    return responses.poll()
                }
            }
        }
    }

    val mockCredential = MockGoogleCredential.Builder()
            .setTransport(MockGoogleCredential.newMockHttpTransportWithSampleTokenResponse())
            .build()

    return Sheets.Builder(mockTransport, JacksonFactory.getDefaultInstance(), mockCredential)
            .setApplicationName("MockedStubbedTestApp")
            .build()
}