package com.example.shop.cart.services

import com.example.shop.cart.domain.CartDomain
import com.example.shop.cart.domain.CartItem
import com.example.shop.cart.models.AddToCartRequest
import com.example.shop.cart.models.RemoveFromCartRequest
import com.example.shop.cart.models.UpdateCartQuantityRequest
import com.example.shop.cart.respositories.CartItemRepository
import com.example.shop.common.apis.exceptions.BadRequestException
import com.example.shop.products.services.ProductService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CartDomainService(
    private val cartService: CartService,
    private val cartItemRepository: CartItemRepository,
    private val productService: ProductService,
) {
    @Transactional
    fun getMyCart(accountId: Long): CartDomain? {
        val cart = cartService.getMyCartNotPurchased(accountId) ?: return null
        val cartItems = cartItemRepository.findAllByCartId(cart.id)

        return CartDomain(cart, cartItems)
    }

    private fun validateProductId(productId: Long) {
        if (productService.isEnabledProductExist(productId)) {
            throw BadRequestException("상품을 찾을 수 없습니다.")
        }
    }

    @Transactional
    fun addItemToCart(request: AddToCartRequest, accountId: Long): CartDomain {
        validateProductId(request.productId)

        val cart = cartService.getOrCreateUnPurchaseCart(accountId)
        val cartItems = cartItemRepository.findAllByCartId(cart.id).toMutableSet()

        val itemAlreadyInCart = cartItems.find { it.productId == request.productId }

        itemAlreadyInCart?.let {
            itemAlreadyInCart.incrementQuantity(request.quantity)
        } ?: run {
            val cartItemToAdd = CartItem(cart, request.productId, request.quantity)
            val savedCartItem = cartItemRepository.save(cartItemToAdd)
            cartItems.add(savedCartItem)
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
            cartService.deleteCart(cart)
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
