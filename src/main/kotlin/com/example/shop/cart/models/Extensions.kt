package com.example.shop.cart.models

import com.example.shop.cart.domain.Cart

fun Cart.toDto(): CartDto {
    val cartItems = this.cartItems?.map {
        CartItemDto(
            cartItemId = it.id!!,
            product = it.product!!,
            quantity = it.quantity!!
        )
    } ?: emptyList()

    return CartDto(
        cartId = this.id!!,
        isPurchased = this.isPurchased,
        cartItems = cartItems
    )
}
