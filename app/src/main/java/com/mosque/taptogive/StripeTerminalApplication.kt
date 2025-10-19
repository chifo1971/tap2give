package com.mosque.taptogive

import android.app.Application
import android.util.Log
import com.stripe.stripeterminal.TerminalApplicationDelegate
import com.stripe.stripeterminal.external.callable.ConnectionTokenProvider
import com.stripe.stripeterminal.external.callable.ConnectionTokenCallback
import com.stripe.stripeterminal.external.models.ConnectionTokenException
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.log.LogLevel
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class StripeTerminalApplication : Application() {

    companion object {
        private const val TAG = "StripeTerminalApp"
        private const val BACKEND_URL = "https://us-central1-tap2give-c8a07.cloudfunctions.net/createConnectionToken"
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize lifecycle delegate
        TerminalApplicationDelegate.onCreate(this)

        // Initialize Terminal SDK
        if (!Terminal.isInitialized()) {
            try {
                Terminal.initTerminal(
                    applicationContext,
                    LogLevel.VERBOSE,
                    TokenProvider(),
                    TerminalEventListener()
                )
                Log.d(TAG, "✅ Terminal initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to initialize Terminal", e)
            }
        }
    }

    // Connection token provider - calls Firebase Function
    private class TokenProvider : ConnectionTokenProvider {
        private val client = OkHttpClient()

        override fun fetchConnectionToken(callback: ConnectionTokenCallback) {
            Log.d(TAG, "Fetching connection token from backend...")

            val request = Request.Builder()
                .url(BACKEND_URL)
                .post(okhttp3.RequestBody.create(null, ByteArray(0))) // Empty POST body
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "Failed to fetch connection token", e)
                    callback.onFailure(
                        ConnectionTokenException("Failed to fetch connection token: ${e.message}", e)
                    )
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val responseBody = response.body?.string()
                        Log.d(TAG, "Backend response: $responseBody")

                        if (!response.isSuccessful) {
                            throw IOException("Unexpected response code: ${response.code}")
                        }

                        if (responseBody == null) {
                            throw IOException("Empty response body")
                        }

                        // Parse JSON response to get the secret
                        val jsonObject = JSONObject(responseBody)
                        val secret = jsonObject.getString("secret")

                        Log.d(TAG, "✅ Connection token received")
                        callback.onSuccess(secret)

                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse connection token", e)
                        callback.onFailure(
                            ConnectionTokenException("Failed to parse token: ${e.message}", e)
                        )
                    }
                }
            })
        }
    }

    // Terminal event listener
    private class TerminalEventListener : com.stripe.stripeterminal.external.callable.TerminalListener {
        override fun onConnectionStatusChange(status: com.stripe.stripeterminal.external.models.ConnectionStatus) {
            Log.d(TAG, "Connection status: $status")
        }

        override fun onPaymentStatusChange(status: com.stripe.stripeterminal.external.models.PaymentStatus) {
            Log.d(TAG, "Payment status: $status")
        }
    }
}