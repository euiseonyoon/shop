package com.example.shop.purchase.services

import com.example.shop.purchase.enums.PurchaseProductStatus
import com.example.shop.purchase.repositories.PurchaseProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class PurchaseProductService(
    private val purchaseProductRepository: PurchaseProductRepository,
) {
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    fun isAllStockUpdated(purchaseId: Long): Boolean {
        val purchaseProducts = purchaseProductRepository.findByPurchaseId(purchaseId)
        return !purchaseProducts.any { it.status == PurchaseProductStatus.READY }
    }
}
