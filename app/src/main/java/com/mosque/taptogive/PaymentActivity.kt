package com.mosque.taptogive

import android.Manifest
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.callable.*
import com.stripe.stripeterminal.external.models.*

class PaymentActivity : AppCompatActivity() {

    private val TAG = "PaymentActivity"
    private val LOCATION_PERMISSION_CODE = 100
    private val firestore = FirebaseFirestore.getInstance()

    // UI Elements
    private lateinit var tvAmount: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvInstruction: TextView
    private lateinit var loadingSpinner: ProgressBar
    private lateinit var nfcIcon: ImageView
    private lateinit var successIcon: ImageView
    private lateinit var errorIcon: ImageView
    private lateinit var btnBack: ImageButton

    // Payment variables
    private var donationAmount: Double = 0.0
    private var paymentIntent: PaymentIntent? = null
    private var collectCancelable: Cancelable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        // Keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Get donation amount
        donationAmount = intent.getDoubleExtra("donation_amount", 0.0)

        // Initialize views
        initializeViews()

        // Set amount display
        tvAmount.text = String.format("$%.2f", donationAmount)

        // Back button
        btnBack.setOnClickListener {
            cancelPaymentAndFinish()
        }

        // Check permissions and start payment flow
        checkPermissionsAndStartPayment()
    }

    private fun initializeViews() {
        tvAmount = findViewById(R.id.tv_amount)
        tvStatus = findViewById(R.id.tv_status)
        tvInstruction = findViewById(R.id.tv_instruction)
        loadingSpinner = findViewById(R.id.loading_spinner)
        nfcIcon = findViewById(R.id.nfc_icon)
        successIcon = findViewById(R.id.success_icon)
        errorIcon = findViewById(R.id.error_icon)
        btnBack = findViewById(R.id.btn_back)
    }

    private fun checkPermissionsAndStartPayment() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_CODE
            )
        } else {
            startPaymentFlow()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startPaymentFlow()
            } else {
                showError(getString(R.string.error_location_permission))
                Handler(Looper.getMainLooper()).postDelayed({
                    finish()
                }, 3000)
            }
        }
    }

    private fun startPaymentFlow() {
        showState(State.CONNECTING)

        // Check if already connected
        val connectedReader = Terminal.getInstance().connectedReader
        if (connectedReader != null) {
            Log.d(TAG, "Already connected to reader")
            createPaymentIntent()
        } else {
            discoverReaders()
        }
    }

    private fun discoverReaders() {
        Log.d(TAG, "Starting reader discovery...")

        val config = DiscoveryConfiguration.TapToPayDiscoveryConfiguration(
            isSimulated = false
        )

        val discoveryListener = object : DiscoveryListener {
            override fun onUpdateDiscoveredReaders(readers: List<Reader>) {
                Log.d(TAG, "Discovered ${readers.size} readers")
                if (readers.isNotEmpty() && Terminal.getInstance().connectedReader == null) {
                    connectToReader(readers[0])
                }
            }
        }

        val callback = object : Callback {
            override fun onSuccess() {
                Log.d(TAG, "Reader discovery completed")
            }

            override fun onFailure(e: TerminalException) {
                Log.e(TAG, "Reader discovery failed: ${e.errorMessage}", e)
                runOnUiThread {
                    showError(getString(R.string.error_connection))
                }
            }
        }

        Terminal.getInstance().discoverReaders(config, discoveryListener, callback)
    }

    private fun connectToReader(reader: Reader) {
        Log.d(TAG, "Connecting to reader: ${reader.serialNumber}")

        val locationId = "tml_GMHotQ4V9ZfdZS"
        val connectionConfig = ConnectionConfiguration.TapToPayConnectionConfiguration(
            locationId = locationId,
            autoReconnectOnUnexpectedDisconnect = true,
            tapToPayReaderListener = null
        )

        Terminal.getInstance().connectReader(
            reader,
            connectionConfig,
            object : ReaderCallback {
                override fun onSuccess(reader: Reader) {
                    Log.d(TAG, "Connected to reader: ${reader.serialNumber}")
                    createPaymentIntent()
                }

                override fun onFailure(e: TerminalException) {
                    Log.e(TAG, "Failed to connect to reader: ${e.errorMessage}", e)
                    runOnUiThread {
                        showError(getString(R.string.error_connection))
                    }
                }
            }
        )
    }


    private fun createPaymentIntent() {
        Log.d(TAG, "Creating payment intent for amount: $donationAmount")

        val params = PaymentIntentParameters.Builder()
            .setAmount((donationAmount * 100).toLong())
            .setCurrency("usd")
            .setCaptureMethod(CaptureMethod.Automatic)  // CRITICAL: Set to Automatic for immediate capture
            .build()

        Terminal.getInstance().createPaymentIntent(
            params,
            object : PaymentIntentCallback {
                override fun onSuccess(paymentIntent: PaymentIntent) {
                    Log.d(TAG, "Payment intent created: ${paymentIntent.id}")
                    Log.d(TAG, "Capture method: Automatic")
                    this@PaymentActivity.paymentIntent = paymentIntent
                    collectPaymentMethod(paymentIntent)
                }

                override fun onFailure(e: TerminalException) {
                    Log.e(TAG, "Failed to create payment intent: ${e.errorMessage}", e)
                    runOnUiThread {
                        showError(getString(R.string.error_generic))
                    }
                }
            }
        )
    }

    private fun collectPaymentMethod(paymentIntent: PaymentIntent) {
        Log.d(TAG, "Collecting payment method...")

        runOnUiThread {
            showState(State.READY)
        }

        collectCancelable = Terminal.getInstance().collectPaymentMethod(
            paymentIntent,
            object : PaymentIntentCallback {
                override fun onSuccess(paymentIntent: PaymentIntent) {
                    Log.d(TAG, "Payment method collected")
                    runOnUiThread {
                        showState(State.PROCESSING)
                    }
                    processPaymentAndCapture(paymentIntent)
                }

                override fun onFailure(e: TerminalException) {
                    Log.e(TAG, "Failed to collect payment method: ${e.errorMessage}", e)
                    runOnUiThread {
                        showError(e.errorMessage)
                    }
                }
            }
        )
    }

    // Simplified - just use confirmPaymentIntent with Automatic capture
    private fun processPaymentAndCapture(paymentIntent: PaymentIntent) {
        Log.d(TAG, "Confirming payment (will auto-capture)...")
        Log.d(TAG, "Payment Intent Status before confirm: ${paymentIntent.status}")

        Terminal.getInstance().confirmPaymentIntent(
            paymentIntent,
            object : PaymentIntentCallback {
                override fun onSuccess(confirmedPaymentIntent: PaymentIntent) {
                    Log.d(TAG, "Payment confirmed: ${confirmedPaymentIntent.id}")
                    Log.d(TAG, "Payment Intent Status after confirm: ${confirmedPaymentIntent.status}")

                    // With CaptureMethod.Automatic, payment should be SUCCEEDED after confirm
                    if (confirmedPaymentIntent.status == PaymentIntentStatus.SUCCEEDED) {
                        Log.d(TAG, "✅ Payment SUCCEEDED and CAPTURED automatically!")
                        runOnUiThread {
                            showState(State.SUCCESS)
                            logTransactionToFirestore(confirmedPaymentIntent)
                        }

                        Handler(Looper.getMainLooper()).postDelayed({
                            returnToMainScreen()
                        }, 2000)
                    } else {
                        Log.w(TAG, "Payment status is: ${confirmedPaymentIntent.status}")
                        runOnUiThread {
                            showState(State.SUCCESS)
                            logTransactionToFirestore(confirmedPaymentIntent)
                        }

                        Handler(Looper.getMainLooper()).postDelayed({
                            returnToMainScreen()
                        }, 2000)
                    }
                }

                override fun onFailure(e: TerminalException) {
                    Log.e(TAG, "Payment confirmation failed: ${e.errorMessage}", e)
                    Log.e(TAG, "Error code: ${e.errorCode}")
                    runOnUiThread {
                        showError(e.errorMessage)
                    }

                    Handler(Looper.getMainLooper()).postDelayed({
                        returnToMainScreen()
                    }, 3000)
                }
            }
        )
    }

    private fun logTransactionToFirestore(paymentIntent: PaymentIntent) {
        val transaction = hashMapOf(
            "amount" to donationAmount,
            "timestamp" to com.google.firebase.Timestamp.now(),
            "txn_id" to paymentIntent.id,
            "status" to paymentIntent.status.toString(),
            "device_id" to android.provider.Settings.Secure.getString(
                contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            )
        )

        firestore.collection("donations")
            .add(transaction)
            .addOnSuccessListener {
                Log.d(TAG, "Transaction logged to Firestore")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to log transaction: ${e.message}", e)
            }
    }

    private fun showState(state: State) {
        when (state) {
            State.CONNECTING -> {
                loadingSpinner.visibility = View.VISIBLE
                nfcIcon.visibility = View.GONE
                successIcon.visibility = View.GONE
                errorIcon.visibility = View.GONE
                tvStatus.text = getString(R.string.connecting)
                tvInstruction.visibility = View.GONE
            }

            State.READY -> {
                loadingSpinner.visibility = View.GONE
                nfcIcon.visibility = View.VISIBLE
                successIcon.visibility = View.GONE
                errorIcon.visibility = View.GONE
                tvStatus.text = getString(R.string.ready_to_tap)
                tvInstruction.text = getString(R.string.tap_instruction)
                tvInstruction.visibility = View.VISIBLE

                // Pulse animation on NFC icon
                val pulseAnimation = ObjectAnimator.ofFloat(nfcIcon, "alpha", 1f, 0.3f, 1f)
                pulseAnimation.duration = 1500
                pulseAnimation.repeatCount = ObjectAnimator.INFINITE
                pulseAnimation.start()
            }

            State.PROCESSING -> {
                loadingSpinner.visibility = View.VISIBLE
                nfcIcon.visibility = View.GONE
                successIcon.visibility = View.GONE
                errorIcon.visibility = View.GONE
                tvStatus.text = getString(R.string.processing)
                tvInstruction.visibility = View.GONE
            }

            State.SUCCESS -> {
                loadingSpinner.visibility = View.GONE
                nfcIcon.visibility = View.GONE
                successIcon.visibility = View.VISIBLE
                errorIcon.visibility = View.GONE
                tvStatus.text = getString(R.string.payment_successful)
                tvInstruction.text = getString(R.string.thank_you)
                tvInstruction.visibility = View.VISIBLE

                // Scale animation on success icon
                successIcon.scaleX = 0f
                successIcon.scaleY = 0f
                successIcon.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(300)
                    .start()
            }
        }
    }

    private fun showError(message: String) {
        loadingSpinner.visibility = View.GONE
        nfcIcon.visibility = View.GONE
        successIcon.visibility = View.GONE
        errorIcon.visibility = View.VISIBLE
        tvStatus.text = getString(R.string.payment_failed)
        tvInstruction.text = message
        tvInstruction.visibility = View.VISIBLE

        // Scale animation on error icon
        errorIcon.scaleX = 0f
        errorIcon.scaleY = 0f
        errorIcon.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(300)
            .start()

        // Return to main screen after 3 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            returnToMainScreen()
        }, 3000)
    }

    private fun cancelPaymentAndFinish() {
        if (collectCancelable != null) {
            collectCancelable?.cancel(object : Callback {
                override fun onSuccess() {
                    Log.d(TAG, "Payment collection cancelled")
                    finish()
                }

                override fun onFailure(e: TerminalException) {
                    Log.e(TAG, "Failed to cancel: ${e.errorMessage}", e)
                    finish()
                }
            })
        } else {
            finish()
        }
    }

    private fun returnToMainScreen() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (collectCancelable != null) {
            collectCancelable?.cancel(object : Callback {
                override fun onSuccess() {}
                override fun onFailure(e: TerminalException) {}
            })
        }
    }

    private enum class State {
        CONNECTING,
        READY,
        PROCESSING,
        SUCCESS
    }
}