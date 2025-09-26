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
    private val cartItemRepository: CartItemRepository,
    private val productRepository: ProductRepository,
) {
    @Transactional
    fun getMyCart(accountId: Long): CartDomain? {
        val cart = cartRepository.getNotPurchasedCartByAccountId(accountId) ?: return null
        val cartItems = cartItemRepository.findAllByCartId(cart.id)

        return CartDomain(cart, cartItems)
    }

    @Transactional
    fun getOrCreateUnPurchaseCart(accountId: Long): Cart {
        val unPurchasedCart = cartRepository.getNotPurchasedCartByAccountId(accountId)
        if (unPurchasedCart != null) {
            return unPurchasedCart
        }

        return cartRepository.save(Cart(accountId = accountId, isPurchased = false))
    }

    private fun validateProductId(productId: Long) {
        if (!productRepository.existsByIdAndIsEnabledTrue(productId)) {
            throw BadRequestException("상품을 찾을 수 없습니다.")
        }
    }

    @Transactional
    fun addItemToCart(request: AddToCartRequest, accountId: Long): CartDomain {
        val cart = getOrCreateUnPurchaseCart(accountId)
        validateProductId(request.productId)

        val cartItems = cartItemRepository.findAllByCartId(cart.id).toMutableSet()

        // 장바구니에 이미 있는 상품인지 확인
        val existingCartItem = cartItems.find { it.productId == request.productId }

        existingCartItem?.let {
            existingCartItem.incrementQuantity(request.quantity)
        } ?: run {
            CartItem(cart, request.productId, request.quantity).let {
                cartItems.add(it)
                cartItemRepository.save(it)
            }
        }

        return CartDomain(cart, cartItems.toList())
    }

    @Transactional
    fun removeItemFromCart(request: RemoveFromCartRequest, accountId: Long): CartDomain? {
        val cartDomain = getMyCart(accountId) ?: return null
        val cart = cartDomain.cart
        val cartItems = cartDomain.cartItems.toMutableSet()
        val cartItemToRemove = cartItems.find { it.productId == request.productId } ?: return cartDomain

        cartItems.remove(cartItemToRemove)
        cartItemRepository.delete(cartItemToRemove)

        return if (cartItems.isEmpty()) {
            cartRepository.delete(cart)
            null
        } else {
            CartDomain(cart, cartItems.toList())
        }
    }

    @Transactional
    fun updateQuantity(
        request: UpdateCartQuantityRequest,
        accountId: Long,
    ): CartDomain? {
        val cartDomain = getMyCart(accountId) ?: return null
        val cartItemToUpdate = cartDomain.cartItems.find { it.productId == request.productId } ?: return cartDomain
        cartItemRepository.save(cartItemToUpdate.incrementQuantity(request.quantity))

        return cartDomain
    }
}
