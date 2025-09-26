package com.example.shop.cart.respositories.extensions

import com.example.shop.cart.domain.Cart
import com.example.shop.cart.domain.QCart
import com.example.shop.cart.domain.QCartItem
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
class CartRepositoryExtensionImpl : QuerydslRepositorySupport(Cart::class.java), CartRepositoryExtension {
    val cart = QCart.cart

    override fun getNotPurchasedCartByAccountId(accountId: Long): Cart? {
        return from(cart)
            .where(cart.isPurchased.eq(false))
            .where(cart.accountId.eq(accountId))
            .fetchOne()
    }
}
