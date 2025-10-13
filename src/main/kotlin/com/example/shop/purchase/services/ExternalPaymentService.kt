package com.example.shop.purchase.services

import com.example.shop.purchase.models.PurchaseApproveRequest
import com.example.shop.purchase.models.pg_payment_result.PaymentApproveResponse
import org.springframework.web.reactive.function.client.WebClient

interface ExternalPaymentService {
    val baseUrl: String
    val paymentApprovePath: String
    val webClient: WebClient

    fun sendPaymentApproveRequest(request: PurchaseApproveRequest): PaymentApproveResponse?
}
