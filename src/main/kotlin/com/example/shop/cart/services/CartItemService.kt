package com.example.shop.cart.services

import com.example.shop.cart.domain.Cart
import com.example.shop.cart.models.AddToCartRequest
import com.example.shop.cart.models.RemoveFromCartRequest
import com.example.shop.cart.models.UpdateCartQuantityRequest
import com.example.shop.cart.respositories.CartRepository
import com.example.shop.common.apis.exceptions.BadRequestException
import com.example.shop.products.respositories.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CartItemService(
    private val cartService: CartService,
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository,
) {
    @Transactional
    fun addItemToCart(request: AddToCartRequest, accountId: Long): Cart {
        val cart = cartService.getOrCreateUnPurchaseCart(accountId)
        if (!productRepository.existsById(request.productId)) {
            throw BadRequestException("Product Not found.")
        }
        // 장바구니에 이미 있는 상품인지 확인
        val existingCartItem = cart.findItem(request.productId)

        if (existingCartItem != null) {
            // 이미 있으면 수량만 증가
            cart.incrementItemQuantity(existingCartItem, request.quantity)
        } else {
            // 없으면 새로운 CartItem 생성 및 Cart에 추가
            cart.addItem(request.productId, request.quantity)
        }

        // `cart`를 저장하면 `cascade` 옵션 덕분에 `cartItems`의 변경사항이 모두 반영됨
        return cartRepository.save(cart)
    }

    @Transactional
    fun removeItemFromCart(request: RemoveFromCartRequest, accountId: Long): Cart? {
        val cart = cartService.getMyCart(accountId) ?: return null
        val cartItemToRemove = cart.findItem(request.productId) ?: return cart

        cart.removeItem(cartItemToRemove)

        return if (cart.items.isEmpty()) {
            cartRepository.delete(cart)
            null
        } else {
            cartRepository.save(cart)
        }
    }

    @Transactional
    fun updateQuantity(
        request: UpdateCartQuantityRequest,
        accountId: Long,
    ): Cart? {
        val cart = cartService.getMyCart(accountId) ?: return null
        val cartItemToUpdate = cart.findItem(request.productId) ?: return cart

        cart.setItemQuantity(cartItemToUpdate, request.quantity)

        return cartRepository.save(cart)
    }
}
