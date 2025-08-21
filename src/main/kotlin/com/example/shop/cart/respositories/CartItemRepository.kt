package com.example.shop.cart.respositories

import com.example.shop.cart.domain.CartItem
import com.example.shop.cart.respositories.extensions.CartItemRepositoryExtension
import org.springframework.data.jpa.repository.JpaRepository

interface CartItemRepository: JpaRepository<CartItem, Long>, CartItemRepositoryExtension {

}
