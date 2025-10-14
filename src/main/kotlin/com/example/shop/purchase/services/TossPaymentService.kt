package com.example.shop.purchase.services

interface TossPaymentService : ExternalPaymentService {
    val secretKey: String

    // NOTE: 추후, Payment response를 확인해야 되는 구현이 있으면 활성화 할 수 있겠다.
    // override fun sendPaymentApproveRequest(request: PurchaseApproveRequest): TossPaymentResponse?
}


