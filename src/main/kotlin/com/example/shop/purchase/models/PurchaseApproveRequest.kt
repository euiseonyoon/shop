package com.example.shop.purchase.models

import jakarta.validation.constraints.Positive

data class PurchaseApproveRequest(
    val paymentKey: String,
    @field:Positive
    val amount: Int,
    val orderId: String,
)

