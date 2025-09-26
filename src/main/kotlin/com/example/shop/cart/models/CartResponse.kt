package com.example.shop.cart.models

import kotlinx.serialization.Serializable

@Serializable
data class CartResponse(
    val cartId: Long,
    val isPurchased: Boolean,
    val items: List<CartItemDto>
)
