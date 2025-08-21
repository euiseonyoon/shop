package com.example.shop.purchase.services

import com.example.shop.purchase.repositories.PurchaseProductRepository
import org.springframework.stereotype.Service

@Service
class PurchaseProductService(
    private val purchaseProductRepository: PurchaseProductRepository
) {

}
