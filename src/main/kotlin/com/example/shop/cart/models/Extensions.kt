package com.example.shop.cart.models

import com.example.shop.cart.domain.CartDomain

fun CartDomain.toDto(): CartResponse {
    return CartResponse(
        cartId = this.cart.id,
        isPurchased = this.cart.isPurchased,
        items = this.cartItems.map { CartItemDto(it.id, it.productId, it.quantity) }
    )
}
