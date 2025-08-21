package com.example.shop.purchase.models

import jakarta.validation.constraints.Positive

data class PurchaseDirectlyRequest(
    val productId: Long,
    @field:Positive
    val quantity: Int,
)

