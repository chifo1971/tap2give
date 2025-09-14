package com.mosque.taptogive

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

class NfcActivity : AppCompatActivity() {
    
    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var pendingIntent: PendingIntent
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Keep screen always on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        // Initialize NFC
        initializeNfc()
    }
    
    private fun initializeNfc() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        
        if (nfcAdapter == null) {
            // NFC not available
            finish()
            return
        }
        
        if (!nfcAdapter.isEnabled) {
            // NFC disabled
            finish()
            return
        }
        
        // Create pending intent for NFC discovery
        pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE
        )
    }
    
    override fun onResume() {
        super.onResume()
        
        if (nfcAdapter != null && nfcAdapter.isEnabled) {
            // Enable NFC discovery
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null)
        }
    }
    
    override fun onPause() {
        super.onPause()
        
        if (nfcAdapter != null) {
            // Disable NFC discovery
            nfcAdapter.disableForegroundDispatch(this)
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        
        // Handle NFC tag discovered
        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        tag?.let { handleNfcTag(it) }
    }
    
    private fun handleNfcTag(tag: Tag) {
        // Handle NFC tag reading
        val isoDep = IsoDep.get(tag)
        
        if (isoDep != null) {
            try {
                isoDep.connect()
                
                // Process the NFC card
                processNfcCard(isoDep)
                
                isoDep.close()
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle error
            }
        }
    }
    
    private fun processNfcCard(isoDep: IsoDep) {
        // Implement NFC card processing logic here
        // This would typically involve:
        // 1. Reading card data
        // 2. Validating the card
        // 3. Processing the payment
        // 4. Returning to the payment activity with result
        
        // For now, just finish and return to payment activity
        val resultIntent = Intent()
        resultIntent.putExtra("nfc_result", "success")
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}
