package com.example.shop.purchase.models.pg_payment_result.toss

import java.time.OffsetDateTime


data class VirtualAccount(
    val accountType: String?,
    val accountNumber: String?,
    val bank: String?,
    val customerName: String?,
    val dueDate: OffsetDateTime?,
    val refundStatus: String?
)

