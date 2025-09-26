package com.example.shop.purchase.repositories

import com.example.shop.purchase.domain.Purchase
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface PurchaseRepository: JpaRepository<Purchase, Long> {
    fun findAllByAccountId(accountId: Long, pageable: Pageable): Page<Purchase>

    fun countByAccountId(accountId: Long): Int

    fun findAllByIdInAndAccountId(purchaseIds: Set<Long>, accountId: Long, pageable: Pageable): Page<Purchase>

    fun countByIdInAndAccountId(purchaseIds: Set<Long>, accountId: Long): Int
}
