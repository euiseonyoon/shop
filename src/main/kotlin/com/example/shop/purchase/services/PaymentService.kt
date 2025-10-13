package com.example.shop.purchase.services

import com.example.shop.purchase.domain.Payment
import com.example.shop.purchase.repositories.PaymentRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class PaymentService(
    private val paymentRepository: PaymentRepository
) {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun savePayment(purchaseId: Long, paymentKey: String): Payment {
        return paymentRepository.save(Payment(purchaseId, paymentKey))
    }
}
