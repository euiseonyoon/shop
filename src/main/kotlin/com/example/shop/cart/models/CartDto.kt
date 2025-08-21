package com.example.shop.cart.models

data class CartDto(
    val cartId: Long,
    val isPurchased: Boolean,
    val cartItems: List<CartItemDto>
)
