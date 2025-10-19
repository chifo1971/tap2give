package com.mosque.taptogive

import android.Manifest
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Request location permissions on app launch
        checkAndRequestLocationPermission()

        // Setup settings button
        val settingsButton = findViewById<TextView>(R.id.btn_settings)
        settingsButton?.setOnClickListener {
            Log.d("MainActivity", "Settings button clicked!")
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        setupDonationButtons()
    }

    private fun checkAndRequestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "Location permission granted")
            } else {
                Log.e("MainActivity", "Location permission denied - Tap to Pay will not work")
            }
        }
    }

    private fun setupDonationButtons() {
        findViewById<MaterialButton>(R.id.btn_10)?.setOnClickListener {
            navigateToPayment(10.0)
        }

        findViewById<MaterialButton>(R.id.btn_25)?.setOnClickListener {
            navigateToPayment(25.0)
        }

        findViewById<MaterialButton>(R.id.btn_50)?.setOnClickListener {
            navigateToPayment(50.0)
        }

        findViewById<MaterialButton>(R.id.btn_100)?.setOnClickListener {
            navigateToPayment(100.0)
        }

        findViewById<MaterialButton>(R.id.btn_custom)?.setOnClickListener {
            showCustomAmountDialog()
        }
    }

    private fun showCustomAmountDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_custom_amount, null)
        val amountInput = dialogView.findViewById<EditText>(R.id.amount_input)
        val amountDisplay = dialogView.findViewById<android.widget.TextView>(R.id.amount_display)

        var currentAmount = ""

        fun updateDisplay() {
            if (currentAmount.isEmpty()) {
                amountDisplay.text = "$0"
            } else {
                amountDisplay.text = "$$currentAmount"
            }
            amountInput.setText(currentAmount)
        }

        val numberButtons = listOf(
            R.id.btn_0 to "0",
            R.id.btn_1 to "1",
            R.id.btn_2 to "2",
            R.id.btn_3 to "3",
            R.id.btn_4 to "4",
            R.id.btn_5 to "5",
            R.id.btn_6 to "6",
            R.id.btn_7 to "7",
            R.id.btn_8 to "8",
            R.id.btn_9 to "9"
        )

        numberButtons.forEach { (buttonId, digit) ->
            dialogView.findViewById<MaterialButton>(buttonId)
                ?.setOnClickListener {
                    currentAmount += digit
                    updateDisplay()
                }
        }

        dialogView.findViewById<MaterialButton>(R.id.btn_clear)
            ?.setOnClickListener {
                currentAmount = ""
                updateDisplay()
            }

        dialogView.findViewById<MaterialButton>(R.id.btn_backspace)
            ?.setOnClickListener {
                if (currentAmount.isNotEmpty()) {
                    currentAmount = currentAmount.dropLast(1)
                    updateDisplay()
                }
            }

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialogView.findViewById<MaterialButton>(R.id.btn_confirm)
            ?.setOnClickListener {
                val amountText = amountInput.text.toString()
                if (amountText.isNotEmpty()) {
                    try {
                        val amount = amountText.toDouble()
                        if (amount > 0) {
                            dialog.dismiss()
                            navigateToPayment(amount)
                        } else {
                            amountDisplay.text = "Must be > $0"
                        }
                    } catch (e: NumberFormatException) {
                        amountDisplay.text = "Invalid amount"
                    }
                } else {
                    amountDisplay.text = "Enter amount"
                }
            }

        dialogView.findViewById<MaterialButton>(R.id.btn_cancel)
            ?.setOnClickListener {
                dialog.dismiss()
            }

        dialog.show()
    }

    private fun navigateToPayment(amount: Double) {
        val intent = Intent(this, PaymentActivity::class.java)
        intent.putExtra("donation_amount", amount)
        startActivity(intent)
    }

    override fun onBackPressed() {
        // Prevent exiting kiosk mode
    }
}