package com.example.shop.cart.services

import com.example.shop.auth.models.AccountAuthenticationToken
import com.example.shop.auth.repositories.AccountRepository
import com.example.shop.cart.domain.Cart
import com.example.shop.cart.domain.CartItem
import com.example.shop.cart.models.AddToCartRequest
import com.example.shop.cart.models.RemoveFromCartRequest
import com.example.shop.cart.models.UpdateCartQuantityRequest
import com.example.shop.cart.respositories.CartItemRepository
import com.example.shop.cart.respositories.CartRepository
import com.example.shop.common.apis.exceptions.BadRequestException
import com.example.shop.products.respositories.ProductRepository
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.function.Supplier

@Service
class CartItemService(
    private val cartService: CartService,
    private val cartRepository: CartRepository,
    private val accountRepository: AccountRepository,
    private val productRepository: ProductRepository,
    private val cartItemRepository: CartItemRepository,
) {
    @Transactional
    fun addItemToCart(request: AddToCartRequest, accountId: Long): Cart {
        val account = accountRepository.findById(accountId).orElseThrow(
            Supplier { BadRequestException("Account Not found.") }
        )
        val cart = cartService.getOrCreateUnPurchaseCart(account)
        val product = productRepository.findById(request.productId).orElseThrow(
            Supplier { BadRequestException("Product Not found.") }
        )
        // 장바구니에 이미 있는 상품인지 확인
        val existingCartItem = cart.cartItems!!.find { it.product?.id == product.id }

        if (existingCartItem != null) {
            // 이미 있으면 수량만 증가
            existingCartItem.quantity = existingCartItem.quantity!! + request.quantity
        } else {
            // 없으면 새로운 CartItem 생성 및 Cart에 추가
            val newCartItem = CartItem().apply {
                this.cart = cart
                this.product = product
                this.quantity = request.quantity
            }
            cart.cartItems!!.add(newCartItem)
        }

        // `cart`를 저장하면 `cascade` 옵션 덕분에 `cartItems`의 변경사항이 모두 반영됨
        return cartRepository.save(cart)
    }

    @Transactional
    fun removeItemFromCart(request: RemoveFromCartRequest, accountId: Long): Cart? {
        val cart = cartService.getMyCart(accountId) ?: return null
        val cartItemToRemove = cartItemRepository.getCartItem(cart.id!!, request.productId) ?: return cart
        cart.cartItems?.remove(cartItemToRemove)

        if (cart.cartItems!!.isEmpty()) {
            cartRepository.delete(cart)
            return null
        }

        return cartRepository.save(cart)
    }

    @Transactional
    fun updateQuantity(
        request: UpdateCartQuantityRequest,
        accountId: Long,
    ): Cart? {
        val cart = cartService.getMyCart(accountId) ?: return null

        val cartItemToUpdate = cart.cartItems!!.find { it.product?.id == request.productId } ?: return cart
        cartItemToUpdate.quantity = request.quantity

        return cartRepository.save(cart)
    }
}
