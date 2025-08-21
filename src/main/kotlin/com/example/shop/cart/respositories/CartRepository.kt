package com.example.shop.cart.respositories

import com.example.shop.cart.domain.Cart
import com.example.shop.cart.respositories.extensions.CartRepositoryExtension
import org.springframework.data.jpa.repository.JpaRepository

interface CartRepository : JpaRepository<Cart, Long>, CartRepositoryExtension {

}
