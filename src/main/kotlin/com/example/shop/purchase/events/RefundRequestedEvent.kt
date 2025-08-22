package com.example.shop.purchase.events

data class RefundRequestedEvent(
    val accountId: Long,
    val purchaseId: Long,
)
