package com.example.shop.cart.models

import kotlinx.serialization.Serializable

@Serializable
data class CartItemDto(
    val cartItemId: Long,
    val productId: Long,
    val quantity: Int,
)
