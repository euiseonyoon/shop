package com.example.shop.refund.repositories

import com.example.shop.refund.domain.Refund
import org.springframework.data.jpa.repository.JpaRepository

interface RefundRepository: JpaRepository<Refund, Long> {
    fun findByPurchaseId(purchaseId: Long): Refund?
}
