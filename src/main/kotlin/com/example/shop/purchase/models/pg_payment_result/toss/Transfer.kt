package com.example.shop.purchase.models.pg_payment_result.toss

data class Transfer(
    val bank: String?,
    val accountNumber: String?,
    val customerName: String?
)
