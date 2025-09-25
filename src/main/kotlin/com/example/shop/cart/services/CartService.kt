package com.example.shop.cart.services

import com.example.shop.auth.domain.Account
import com.example.shop.auth.models.AccountAuthenticationToken
import com.example.shop.cart.domain.Cart
import com.example.shop.cart.respositories.CartRepository
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CartService(
    private val cartRepository: CartRepository,
) {
    @Transactional
    fun getMyCart(accountId: Long): Cart? = cartRepository.getNotPurchasedCartByAccountId(accountId)

    @Transactional
    fun getOrCreateUnPurchaseCart(accountId: Long): Cart {
        val unPurchasedCart = cartRepository.getNotPurchasedCartByAccountId(accountId)
        if (unPurchasedCart != null) {
            return unPurchasedCart
        }

        return cartRepository.save(Cart(accountId = accountId, isPurchased = false))
    }
}
