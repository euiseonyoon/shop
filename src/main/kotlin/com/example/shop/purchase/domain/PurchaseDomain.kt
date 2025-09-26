package com.example.shop.purchase.domain

data class PurchaseDomain(
    val purchase: Purchase,
    val purchaseProducts: List<PurchaseProduct>
)
