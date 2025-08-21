package com.example.shop.cart.respositories.extensions

import com.example.shop.cart.domain.CartItem
import com.example.shop.cart.domain.QCartItem
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
class CartItemRepositoryExtensionImpl : QuerydslRepositorySupport(CartItem::class.java), CartItemRepositoryExtension {
    val cartItem = QCartItem.cartItem

    override fun getCartItem(cartId: Long, productId: Long): CartItem? {
        return from(cartItem)
            .where(cartItem.cart.id.eq(cartId))
            .where(cartItem.product.id.eq(productId))
            .fetchOne()
    }
}
