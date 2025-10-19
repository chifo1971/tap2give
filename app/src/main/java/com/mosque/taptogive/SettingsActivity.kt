package com.mosque.taptogive

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    private lateinit var paymentModeSwitch: SwitchMaterial
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Setup toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"

        // Initialize views
        paymentModeSwitch = findViewById(R.id.switch_payment_mode)
        statusText = findViewById(R.id.tv_payment_status)

        // Load saved setting - IMPORTANT: Using "app_settings" to match PaymentActivity
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val isRealPayment = prefs.getBoolean("real_payment_enabled", false)

        // Set initial state
        paymentModeSwitch.isChecked = isRealPayment
        updateStatusText(isRealPayment)

        // Save setting when toggled
        paymentModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Save to preferences - Using same key as PaymentActivity
            prefs.edit().putBoolean("real_payment_enabled", isChecked).apply()

            // Update status text
            updateStatusText(isChecked)

            // Show toast
            val mode = if (isChecked) "Real Payment Mode" else "Simulation Mode"
            Toast.makeText(
                this,
                "$mode Enabled",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateStatusText(isRealPayment: Boolean) {
        statusText.text = if (isRealPayment) {
            "Currently: Real NFC Payments"
        } else {
            "Currently: Simulated Payments"
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}