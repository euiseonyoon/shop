package com.example.shop.cart.models

import com.example.shop.products.domain.Product

data class CartItemDto(
    val cartItemId: Long,
    val productId: Long,
    val quantity: Int,
)
