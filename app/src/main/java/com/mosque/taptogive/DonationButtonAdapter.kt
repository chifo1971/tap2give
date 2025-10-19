package com.mosque.taptogive

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class DonationButtonAdapter(
    private val onAmountClicked: (DonationAmount) -> Unit
) : RecyclerView.Adapter<DonationButtonAdapter.DonationButtonViewHolder>() {

    private val donationAmounts = listOf(
        DonationAmount(5.0, "$5"),
        DonationAmount(10.0, "$10"),
        DonationAmount(25.0, "$25"),
        DonationAmount(50.0, "$50"),
        DonationAmount(100.0, "$100"),
        DonationAmount(0.0, "Custom Amount", true)
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonationButtonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_donation_button, parent, false)
        return DonationButtonViewHolder(view as MaterialButton)
    }

    override fun onBindViewHolder(holder: DonationButtonViewHolder, position: Int) {
        holder.bind(donationAmounts[position])
    }

    override fun getItemCount(): Int = donationAmounts.size

    inner class DonationButtonViewHolder(
        private val button: MaterialButton
    ) : RecyclerView.ViewHolder(button) {

        fun bind(donationAmount: DonationAmount) {
            button.text = donationAmount.displayText
            button.contentDescription = if (donationAmount.isCustom) {
                "Donate custom amount"
            } else {
                "Donate ${donationAmount.displayText}"
            }
            
            button.setOnClickListener {
                onAmountClicked(donationAmount)
            }
        }
    }
}
