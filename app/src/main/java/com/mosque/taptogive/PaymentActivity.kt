package com.mosque.taptogive

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class PaymentActivity : AppCompatActivity() {

    private lateinit var amountTextView: TextView
    private lateinit var cardPaymentButton: MaterialButton
    private lateinit var nfcPaymentButton: MaterialButton
    private lateinit var cancelButton: MaterialButton

    private var donationAmount: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        // Keep screen always on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Get donation amount from intent
        donationAmount = intent.getDoubleExtra("donation_amount", 0.0)

        // Initialize views
        initializeViews()

        // Display amount
        updateAmountDisplay()

        // Set up button listeners
        setupButtonListeners()
    }

    private fun initializeViews() {
        amountTextView = findViewById(R.id.payment_amount)
        cardPaymentButton = findViewById(R.id.btn_card_payment)
        nfcPaymentButton = findViewById(R.id.btn_nfc_payment)
        cancelButton = findViewById(R.id.btn_back)
        
        // Debug logs to confirm button detection
        Log.d("PaymentDebug", "Card button found: ${cardPaymentButton != null}")
        Log.d("PaymentDebug", "NFC button found: ${nfcPaymentButton != null}")
    }

    private fun updateAmountDisplay() {
        val amountText = if (donationAmount == 0.0) {
            "Custom Amount"
        } else {
            String.format("$%.2f", donationAmount)
        }
        amountTextView.text = getString(R.string.payment_amount, amountText)
    }

    private fun setupButtonListeners() {
        cardPaymentButton.setOnClickListener {
            Log.d("PaymentDebug", "Card button clicked!")
            simulatePaymentSuccess("Card payment simulated successfully!")
        }
        
        nfcPaymentButton.setOnClickListener {
            Log.d("PaymentDebug", "NFC button clicked!")
            simulatePaymentSuccess("NFC payment simulated successfully!")
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun simulatePaymentSuccess(message: String) {
        Toast.makeText(this, "$message Amount: $${String.format("%.2f", donationAmount)}", Toast.LENGTH_LONG).show()

        // Return to main screen after brief delay
        amountTextView.postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }, 1500)
    }

    override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}