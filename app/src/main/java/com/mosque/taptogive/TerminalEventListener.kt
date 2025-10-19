package com.mosque.taptogive

import com.stripe.stripeterminal.external.callable.TerminalListener
import com.stripe.stripeterminal.external.models.ConnectionStatus
import com.stripe.stripeterminal.external.models.PaymentStatus
import com.stripe.stripeterminal.external.models.Reader

class TerminalEventListener : TerminalListener {

    override fun onConnectionStatusChange(status: ConnectionStatus) {
        android.util.Log.d("StripeTerminal", "Connection status: $status")
    }

    override fun onPaymentStatusChange(status: PaymentStatus) {
        android.util.Log.d("StripeTerminal", "Payment status: $status")
    }
}