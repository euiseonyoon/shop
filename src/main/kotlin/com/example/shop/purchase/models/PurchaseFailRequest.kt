package com.example.shop.purchase.models

data class PurchaseFailRequest(
    val errorCode: String,
    val errorMessage: String,
    val orderId: String,
)
