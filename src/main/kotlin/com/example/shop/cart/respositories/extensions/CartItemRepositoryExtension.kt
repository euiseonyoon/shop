package com.example.shop.cart.respositories.extensions

import com.example.shop.cart.domain.CartItem

interface CartItemRepositoryExtension {
    fun getCartItem(cartId: Long, productId: Long): CartItem?
}
