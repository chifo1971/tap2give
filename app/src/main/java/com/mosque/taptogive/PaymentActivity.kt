package com.mosque.taptogive

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class PaymentActivity : AppCompatActivity() {
    
    private lateinit var amountTextView: TextView
    private lateinit var processButton: MaterialButton
    private lateinit var cancelButton: MaterialButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)
        
        // Keep screen always on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        // Get donation amount from intent
        val amount = intent.getDoubleExtra("donation_amount", 0.0)
        
        // Initialize views
        amountTextView = findViewById(R.id.payment_amount)
        processButton = findViewById(R.id.btn_card_payment)
        cancelButton = findViewById(R.id.btn_back)
        
        // Display amount
        val amountText = if (amount == 0.0) {
            "Custom Amount"
        } else {
            String.format("$%.2f", amount)
        }
        amountTextView.text = getString(R.string.payment_amount, amountText)
        
        // Set up button listeners
        processButton.setOnClickListener {
            // TODO: Implement Stripe Terminal integration later
            // For now, just simulate success
            simulatePaymentSuccess()
        }
        
        cancelButton.setOnClickListener {
            finish()
        }
    }
    
    private fun simulatePaymentSuccess() {
        // TODO: Replace with actual payment processing
        Toast.makeText(this, "Payment simulated successfully!", Toast.LENGTH_SHORT).show()
        
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }
    
    override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}