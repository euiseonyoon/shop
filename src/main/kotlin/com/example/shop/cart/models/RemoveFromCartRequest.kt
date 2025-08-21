package com.example.shop.cart.models

import kotlinx.serialization.Serializable

@Serializable
data class RemoveFromCartRequest(
    val productId: Long,
)
