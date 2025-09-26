package com.example.shop.purchase.repositories

import com.example.shop.purchase.domain.PurchaseProduct
import org.springframework.data.jpa.repository.JpaRepository

interface PurchaseProductRepository : JpaRepository<PurchaseProduct, Long> {
    fun findAllByPurchaseIdIn(purchaseIds: List<Long>): List<PurchaseProduct>
}
