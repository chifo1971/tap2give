package com.mosque.taptogive

import android.app.Dialog
import android.content.Intent
import android.content.res.Resources
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.remoteconfig.FirebaseRemoteConfig

class MainActivity : AppCompatActivity() {
    
    // Firebase
    private lateinit var firestore: FirebaseFirestore
    private lateinit var remoteConfig: FirebaseRemoteConfig
    
    // Sound effects
    private lateinit var soundPool: SoundPool
    private var buttonClickSoundId = 0
    private var successSoundId = 0
    private var errorSoundId = 0
    
    // UI Components
    private lateinit var donationGrid: RecyclerView
    private lateinit var loadingIndicator: View
    private lateinit var donationAdapter: DonationButtonAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize Firebase
        initializeFirebase()
        
        // Initialize sound effects
        initializeSoundEffects()
        
        // Initialize UI
        initializeUI()
        
        // Load remote configuration
        loadRemoteConfig()
    }
    
    private fun initializeFirebase() {
        firestore = FirebaseFirestore.getInstance()
        remoteConfig = FirebaseRemoteConfig.getInstance()
        
        // Set default values for remote config (optional)
        try {
            val configDefaults = mapOf(
                "donation_amounts" to listOf(5, 10, 25, 50, 100),
                "primary_color" to "#04A7A9",
                "secondary_color" to "#FFD700"
            )
            remoteConfig.setDefaultsAsync(configDefaults)
        } catch (e: Exception) {
            // Remote config is optional, continue without it
            // The app will use hardcoded values
        }
    }
    
    private fun initializeSoundEffects() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
            
        soundPool = SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(audioAttributes)
            .build()
        
        // TODO: Add actual audio files to res/raw/ directory
        // buttonClickSoundId = soundPool.load(this, R.raw.button_click, 1)
        // successSoundId = soundPool.load(this, R.raw.success, 1)
        // errorSoundId = soundPool.load(this, R.raw.error, 1)

        // Temporary: Set to 0 to avoid crashes
        buttonClickSoundId = 0
        successSoundId = 0
        errorSoundId = 0
    }
    
    private fun initializeUI() {
        // Get screen dimensions for responsive sizing
        val displayMetrics = resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels
        val screenWidth = displayMetrics.widthPixels
        
        // Initialize RecyclerView
        donationGrid = findViewById(R.id.donation_grid)
        loadingIndicator = findViewById(R.id.loading_indicator)
        
        // Calculate optimal button height based on screen size
        val buttonHeight = calculateOptimalButtonHeight(screenHeight)
        
        // Set up RecyclerView with responsive grid
        val gridLayoutManager = GridLayoutManager(this, 2)
        donationGrid.layoutManager = gridLayoutManager
        
        // Create and set adapter
        donationAdapter = DonationButtonAdapter { donationAmount ->
            onDonationAmountClicked(donationAmount)
        }
        donationGrid.adapter = donationAdapter
        
        // Apply responsive sizing to buttons
        applyResponsiveSizing(buttonHeight)
    }
    
    private fun calculateOptimalButtonHeight(screenHeight: Int): Int {
        // Calculate button height as percentage of screen height
        // Reserve space for title (20%) and padding (10%), use remaining 70% for buttons
        val availableHeight = (screenHeight * 0.7).toInt()
        val buttonHeight = availableHeight / 3 // 3 rows of buttons (2x3 grid)
        return maxOf(buttonHeight, 120) // Minimum 120dp height
    }
    
    private fun applyResponsiveSizing(buttonHeight: Int) {
        // This will be applied through the adapter's item layout
        // The ResponsiveDonationButton style already handles this
    }
    
    private fun loadRemoteConfig() {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Apply remote configuration if needed
                    applyRemoteConfig()
                } else {
                    // Use default values
                    applyDefaultConfig()
                }
            }
    }
    
    private fun applyRemoteConfig() {
        // You can use remote config to customize donation amounts, colors, etc.
        // For now, we'll use the default values
    }
    
    private fun applyDefaultConfig() {
        // Use default configuration
    }
    
    private fun onDonationAmountClicked(donationAmount: DonationAmount) {
        playButtonClickSound()
        
        if (donationAmount.isCustom) {
            showCustomAmountDialog()
        } else {
            navigateToPayment(donationAmount.amount)
        }
    }
    
    private fun navigateToPayment(amount: Double) {
        val intent = Intent(this, PaymentActivity::class.java)
        intent.putExtra("donation_amount", amount)
        startActivity(intent)
    }
    
    private fun playButtonClickSound() {
        if (buttonClickSoundId > 0) {
            soundPool.play(buttonClickSoundId, 1.0f, 1.0f, 0, 0, 1.0f)
        }
    }
    
    private fun playSuccessSound() {
        if (successSoundId > 0) {
            soundPool.play(successSoundId, 1.0f, 1.0f, 0, 0, 1.0f)
        }
    }
    
    private fun playErrorSound() {
        if (errorSoundId > 0) {
            soundPool.play(errorSoundId, 1.0f, 1.0f, 0, 0, 1.0f)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }
    
    // Handle back button press - prevent exiting kiosk mode
    override fun onBackPressed() {
        // Do nothing to prevent exiting kiosk mode
        // In a real kiosk, you might want to show an admin dialog
    }
    
    private fun showCustomAmountDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_custom_amount)
        dialog.setCancelable(false)
        
        // Get views
        val amountDisplay = dialog.findViewById<TextView>(R.id.amount_display)
        val btnCancel = dialog.findViewById<MaterialButton>(R.id.btn_cancel)
        val btnConfirm = dialog.findViewById<MaterialButton>(R.id.btn_confirm)
        
        // Number buttons
        val btn0 = dialog.findViewById<Button>(R.id.button0)
        val btn1 = dialog.findViewById<Button>(R.id.button1)
        val btn2 = dialog.findViewById<Button>(R.id.button2)
        val btn3 = dialog.findViewById<Button>(R.id.button3)
        val btn4 = dialog.findViewById<Button>(R.id.button4)
        val btn5 = dialog.findViewById<Button>(R.id.button5)
        val btn6 = dialog.findViewById<Button>(R.id.button6)
        val btn7 = dialog.findViewById<Button>(R.id.button7)
        val btn8 = dialog.findViewById<Button>(R.id.button8)
        val btn9 = dialog.findViewById<Button>(R.id.button9)
        val btnClear = dialog.findViewById<Button>(R.id.buttonClear)
        val btnDelete = dialog.findViewById<Button>(R.id.buttonDelete)
        
        // Debug: Check if buttons are found
        android.util.Log.d("CustomAmountDialog", "Button 1 found: ${btn1 != null}")
        android.util.Log.d("CustomAmountDialog", "Button 0 found: ${btn0 != null}")
        android.util.Log.d("CustomAmountDialog", "Clear button found: ${btnClear != null}")
        
        // Current amount being entered (whole dollars only)
        var currentAmount = 0
        
        // Update display
        fun updateDisplay() {
            amountDisplay.text = "$$currentAmount"
        }
        
        // Add digit to amount
        fun addDigit(digit: Int) {
            android.util.Log.d("CustomAmountDialog", "addDigit called with: $digit")
            playButtonClickSound()
            // Prevent amounts over $9999
            if (currentAmount < 9999) {
                currentAmount = currentAmount * 10 + digit
                updateDisplay()
            }
        }
        
        // Clear amount
        fun clearAmount() {
            android.util.Log.d("CustomAmountDialog", "clearAmount called")
            playButtonClickSound()
            currentAmount = 0
            updateDisplay()
        }
        
        // Delete last digit
        fun deleteDigit() {
            android.util.Log.d("CustomAmountDialog", "deleteDigit called")
            playButtonClickSound()
            currentAmount = currentAmount / 10
            updateDisplay()
        }
        
        // Set up number button listeners
        btn0.setOnClickListener { addDigit(0) }
        btn1.setOnClickListener { addDigit(1) }
        btn2.setOnClickListener { addDigit(2) }
        btn3.setOnClickListener { addDigit(3) }
        btn4.setOnClickListener { addDigit(4) }
        btn5.setOnClickListener { addDigit(5) }
        btn6.setOnClickListener { addDigit(6) }
        btn7.setOnClickListener { addDigit(7) }
        btn8.setOnClickListener { addDigit(8) }
        btn9.setOnClickListener { addDigit(9) }
        
        // Clear button
        btnClear.setOnClickListener { clearAmount() }
        
        // Delete button
        btnDelete.setOnClickListener { deleteDigit() }
        
        // Cancel button
        btnCancel.setOnClickListener {
            playButtonClickSound()
            dialog.dismiss()
        }
        
        // Confirm button
        btnConfirm.setOnClickListener {
            playButtonClickSound()
            if (currentAmount > 0) {
                val amount = currentAmount.toDouble()
                dialog.dismiss()
                navigateToPayment(amount)
            } else {
                Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Initialize display
        updateDisplay()
        
        // Show dialog
        dialog.show()
    }
}