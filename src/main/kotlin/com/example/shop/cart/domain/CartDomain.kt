package com.example.shop.cart.domain

data class CartDomain(
    val cart: Cart,
    val cartItems: List<CartItem>,
)
