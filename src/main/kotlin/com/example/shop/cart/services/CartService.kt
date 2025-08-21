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
    fun saveCart(cart: Cart): Cart = cartRepository.save(cart)

    @Transactional
    fun getMyCart(authentication: Authentication): Cart? {
        val auth = (authentication as AccountAuthenticationToken)
        return cartRepository.getNotPurchasedCartByAccountId(auth.accountId)
    }

    @Transactional
    fun getOrCreateUnPurchaseCart(account: Account) : Cart {
        val unPurchasedCart = cartRepository.getNotPurchasedCartByAccountId(account.id!!)
        if (unPurchasedCart != null) {
            return unPurchasedCart
        }

        return cartRepository.save(Cart().apply { this.account = account })
    }
}
