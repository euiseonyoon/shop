package com.example.shop.purchase.services

import com.example.shop.purchase.domain.Purchase
import com.example.shop.purchase.models.PurchaseApproveRequest
import com.example.shop.purchase.models.PurchaseApproveResult

interface PurchaseApproveHelper {
    val maxStockUpdatedTrial: Int
    val stockUpdatedCheckIntervalMilliSeconds: Long

    fun approveByPurchaseStatus(purchase: Purchase, request: PurchaseApproveRequest): PurchaseApproveResult
}
