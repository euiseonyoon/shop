package com.example.shop.cart.respositories

import com.example.shop.cart.domain.CartItem
import org.springframework.data.jpa.repository.JpaRepository

interface CartItemRepository : JpaRepository<CartItem, Long> {
    fun findAllByCartId(cartId: Long): List<CartItem>
}
