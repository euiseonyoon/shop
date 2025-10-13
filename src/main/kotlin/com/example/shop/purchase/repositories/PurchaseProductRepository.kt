package com.example.shop.purchase.repositories

import com.example.shop.purchase.domain.PurchaseProduct
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PurchaseProductRepository : JpaRepository<PurchaseProduct, Long> {
    fun findByPurchaseId(purchaseId: Long): List<PurchaseProduct>

    fun findAllByPurchaseIdIn(purchaseIds: List<Long>): List<PurchaseProduct>

    @Query("SELECT pp FROM PurchaseProduct pp JOIN FETCH pp.purchase WHERE pp.id = :purchaseProductId")
    fun findByIdWithPurchase(purchaseProductId: Long): PurchaseProduct?
}
