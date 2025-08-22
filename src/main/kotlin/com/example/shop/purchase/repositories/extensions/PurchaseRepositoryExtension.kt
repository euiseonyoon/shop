package com.example.shop.purchase.repositories.extensions

import com.example.shop.purchase.domain.Purchase
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface PurchaseRepositoryExtension {
    fun searchWithPurchaseProduct(purchaseIds: List<Long>?, accountId: Long, pageable: Pageable): Page<Purchase>

    fun searachAccountPurchase(purchaseId: Long, accountId: Long): Purchase?
}
