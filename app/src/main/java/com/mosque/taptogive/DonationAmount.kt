package com.mosque.taptogive

data class DonationAmount(
    val amount: Double,
    val displayText: String,
    val isCustom: Boolean = false
)
