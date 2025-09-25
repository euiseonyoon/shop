package com.example.shop.cart.models

import com.example.shop.cart.domain.Cart

fun Cart.toDto(): CartDto {
    return CartDto(
        cartId = this.id,
        isPurchased = this.isPurchased,
        items = items.map { CartItemDto(it.id, it.productId, it.quantity) }
    )
}
