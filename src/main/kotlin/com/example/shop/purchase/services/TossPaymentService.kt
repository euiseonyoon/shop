package com.example.shop.purchase.services

import com.example.shop.purchase.models.PurchaseApproveRequest
import com.example.shop.purchase.models.pg_payment_result.toss.TossPaymentResponse

interface TossPaymentService : ExternalPaymentService {
    val secretKey: String

    override fun sendPaymentApproveRequest(request: PurchaseApproveRequest): TossPaymentResponse?
}


