package com.example.shop.purchase.models.pg_payment_result.toss

import com.example.shop.purchase.models.pg_payment_result.PaymentApproveResponse
import java.time.OffsetDateTime

data class TossPaymentResponse(
    val version: String,
    val paymentKey: String,
    val type: String,
    val orderId: String,
    val orderName: String,
    val method: String,
    val totalAmount: Int,
    val balanceAmount: Int,
    val status: String,
    val requestedAt: OffsetDateTime,
    val approvedAt: OffsetDateTime?,
    val failedAt: OffsetDateTime?,
    val canceledAt: OffsetDateTime?,
    val lastTransactionKey: String?,
    val cancelAvailableAmount: Int?,
    val card: Card? = null,
    val virtualAccount: VirtualAccount? = null,
    val transfer: Transfer? = null,
    val easyPay: EasyPay? = null
) : PaymentApproveResponse
