package com.example.shop.refund.repositories

import com.example.shop.refund.domain.Refund

interface RefundRepositoryExtension {
    fun searchByPurchaseId(purchaseId: Long): Refund?
}
