package com.example.shop.purchase.services

import com.example.shop.auth.models.AccountAuthenticationToken
import com.example.shop.auth.repositories.AccountRepository
import com.example.shop.cart.services.CartService
import com.example.shop.common.apis.exceptions.BadRequestException
import com.example.shop.products.respositories.ProductRepository
import com.example.shop.purchase.domain.Purchase
import com.example.shop.purchase.domain.PurchaseProduct
import com.example.shop.purchase.models.PurchaseDirectlyRequest
import com.example.shop.purchase.repositories.PurchaseRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.function.Supplier

@Service
class PurchaseService(
    private val purchaseRepository: PurchaseRepository,
    private val accountRepository: AccountRepository,
    private val productRepository: ProductRepository,
    private val cartService: CartService,
) {
    @Transactional(readOnly = true)
    fun getMyPurchases(
        purchaseIds: List<Long>?,
        authentication: Authentication,
        pageable: Pageable,
    ): Page<Purchase> {
        val auth = authentication as AccountAuthenticationToken
        return purchaseRepository.searchWithPurchaseProduct(purchaseIds, auth.accountId, pageable)
    }

    @Transactional
    fun purchaseDirectly(request: PurchaseDirectlyRequest, authentication: Authentication): Purchase {
        val auth = authentication as AccountAuthenticationToken
        val account = accountRepository.findById(auth.accountId).orElseThrow(
            Supplier { BadRequestException("Account Not found.") }
        )
        val purchase = Purchase().apply { this.account = account }

        val product = productRepository.findById(request.productId).orElseThrow(
            Supplier { BadRequestException("Product Not found.") }
        )
        product.stock = product.stock!! - request.quantity
        val savedProduct = productRepository.save(product)

        val purchaseProduct = PurchaseProduct().apply {
            this.quantity = request.quantity
            this.product = savedProduct
        }

        purchase.addPurchaseProducts(listOf(purchaseProduct))
        return purchaseRepository.save(purchase)
    }

    @Transactional
    fun purchaseByCart(authentication: Authentication): Purchase? {
        val cart = cartService.getMyCart(authentication) ?: return null
        val purchaseProducts = cart.cartItems!!.map { cartItem ->
            cartItem.product!!.stock = cartItem.product!!.stock!! - cartItem.quantity!!
            val savedProduct = productRepository.save(cartItem.product!!)

            PurchaseProduct().apply {
                this.product = savedProduct
                this.quantity = cartItem.quantity!!
            }
        }
        val purchase = Purchase().apply {
            this.account = cart.account!!
        }
        purchase.addPurchaseProducts(purchaseProducts)

        cart.isPurchased = true
        cartService.saveCart(cart)

        return purchaseRepository.save(purchase)
    }
}
