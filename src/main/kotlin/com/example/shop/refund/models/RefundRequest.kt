package com.example.shop.refund.models

data class RefundRequest(
    val purchaseId: Long,
    val reason: String?
)

