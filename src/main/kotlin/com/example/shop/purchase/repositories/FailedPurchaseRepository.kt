package com.example.shop.purchase.repositories

import com.example.shop.purchase.domain.FailedPurchase
import org.springframework.data.jpa.repository.JpaRepository

interface FailedPurchaseRepository : JpaRepository<FailedPurchase, Long> {}