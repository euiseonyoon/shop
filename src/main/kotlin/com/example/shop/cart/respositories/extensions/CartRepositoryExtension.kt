package com.example.shop.cart.respositories.extensions

import com.example.shop.cart.domain.Cart

interface CartRepositoryExtension {
    fun getNotPurchasedCartByAccountId(accountId: Long): Cart?
}
