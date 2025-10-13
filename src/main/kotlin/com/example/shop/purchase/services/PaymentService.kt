package com.example.shop.purchase.services

import com.example.shop.purchase.domain.Payment
import com.example.shop.purchase.repositories.PaymentRepository
import org.springframework.stereotype.Component

@Component
class PaymentService(
    private val paymentRepository: PaymentRepository
) {
    fun savePayment(purchaseId: Long, paymentKey: String): Payment {
        return paymentRepository.save(Payment(purchaseId, paymentKey))
    }
}
