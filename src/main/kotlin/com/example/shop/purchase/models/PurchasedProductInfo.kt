package com.example.shop.purchase.models

import kotlinx.serialization.Serializable

@Serializable
data class PurchasedProductInfo(
    val productId: Long,
    val count: Int,
)
