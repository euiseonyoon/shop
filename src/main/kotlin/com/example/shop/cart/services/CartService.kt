package com.example.shop.cart.services

import com.example.shop.cart.domain.Cart
import com.example.shop.cart.domain.CartDomain
import com.example.shop.cart.domain.CartItem
import com.example.shop.cart.models.AddToCartRequest
import com.example.shop.cart.models.RemoveFromCartRequest
import com.example.shop.cart.models.UpdateCartQuantityRequest
import com.example.shop.cart.respositories.CartItemRepository
import com.example.shop.cart.respositories.CartRepository
import com.example.shop.common.apis.exceptions.BadRequestException
import com.example.shop.products.respositories.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CartService(
    private val cartRepository: CartRepository,
) {
    @Transactional
    fun getMyCartNotPurchased(accountId: Long): Cart? {
        return cartRepository.getNotPurchasedCartByAccountId(accountId) ?: return null
    }

    @Transactional
    fun deleteCart(cart: Cart) {
        cartRepository.delete(cart)
    }

    @Transactional
    fun getOrCreateUnPurchaseCart(accountId: Long): Cart {
        val unPurchasedCart = cartRepository.getNotPurchasedCartByAccountId(accountId)
        if (unPurchasedCart != null) {
            return unPurchasedCart
        }

        return cartRepository.save(Cart(accountId = accountId, isPurchased = false))
    }
}
