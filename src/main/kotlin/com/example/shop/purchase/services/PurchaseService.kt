package com.example.shop.purchase.services

import com.example.shop.auth.models.AccountAuthenticationToken
import com.example.shop.cart.domain.CartItem
import com.example.shop.cart.services.CartService
import com.example.shop.common.apis.exceptions.NotFoundException
import com.example.shop.products.domain.Product
import com.example.shop.products.services.ProductService
import com.example.shop.purchase.domain.Purchase
import com.example.shop.purchase.domain.PurchaseProduct
import com.example.shop.purchase.exceptions.PurchaseByCartException
import com.example.shop.purchase.models.PurchaseDirectlyRequest
import com.example.shop.purchase.repositories.PurchaseRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PurchaseService(
    private val purchaseRepository: PurchaseRepository,
    private val productService: ProductService,
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
    fun purchaseDirectly(request: PurchaseDirectlyRequest, accountId: Long): Purchase {
        val purchase = Purchase(accountId)
        val product = productService.findById(request.productId)?.decrementStock(request.quantity)
            ?: throw NotFoundException("Product Not found.")

        purchase.addPurchaseProduct(PurchaseProduct(purchase, product.id, request.quantity))

        return purchaseRepository.save(purchase)
    }

    @Transactional
    fun purchaseByCart(accountId: Long): Purchase? {
        val cart = cartService.getMyCart(accountId) ?: return null
        val productsInCart = productService.findByIds(cart.items.map { it.productId })

        val purchase = Purchase(cart.accountId).also {
            it.addPurchaseProducts(makePurchaseProducts(it, cart.items, productsInCart))
        }

        cart.isPurchased = true

        return purchaseRepository.save(purchase)
    }

    private fun makePurchaseProducts(
        purchase: Purchase,
        cartItems: List<CartItem>,
        productsInCart: List<Product>
    ): List<PurchaseProduct> {
        val productIdMap: Map<Long, Product> = productsInCart.associateBy { it.id!! }

        val validationErrors = mutableMapOf<Long, String>()
        val purchaseProducts = mutableListOf<PurchaseProduct>()

        cartItems.forEach { cartItem ->
            val product = productIdMap[cartItem.productId]
            when {
                product == null -> validationErrors[cartItem.productId] = "상품을 찾을 수 없습니다."
                product.isStockInsufficient(cartItem.quantity) ->
                    validationErrors[cartItem.productId] = "상품의 재고 수량이 부족합니다."
                else -> {
                    purchaseProducts.add(
                        PurchaseProduct(purchase, product.decrementStock(cartItem.quantity).id, cartItem.quantity)
                    )
                }
            }
        }

        throwPurchaseByCartException(validationErrors)
        return purchaseProducts
    }

    private fun throwPurchaseByCartException(validationErrors: Map<Long, String>) {
        if (validationErrors.isNotEmpty()) {
            val baseMessage = "장바구니 구매 오류. 상세 원인은 다음과 같습니다."
            val errorDetails = validationErrors.entries.joinToString(separator = ", ") { (productId, reason) ->
                "상품ID: ${productId}, 원인: ${reason}"
            }
            throw PurchaseByCartException("$baseMessage $errorDetails")
        }
    }
}
