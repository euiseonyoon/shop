package com.example.shop.purchase.services

import com.example.shop.auth.models.AccountAuthenticationToken
import com.example.shop.cart.domain.CartItem
import com.example.shop.cart.services.CartService
import com.example.shop.common.apis.exceptions.BadRequestException
import com.example.shop.products.domain.Product
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
    fun purchaseDirectly(request: PurchaseDirectlyRequest, accountId: Long): Purchase {
        val purchase = Purchase(accountId)
        val product = productRepository.findById(request.productId).orElseThrow(
            Supplier { BadRequestException("Product Not found.") }
        )
        product.stock = product.stock!! - request.quantity
        productRepository.save(product).also {
            purchase.addPurchaseProduct(PurchaseProduct(purchase, it.id!!, request.quantity))
        }

        return purchaseRepository.save(purchase)
    }

    @Transactional
    fun purchaseByCart(accountId: Long): Purchase? {
        val cart = cartService.getMyCart(accountId) ?: return null
        val productsInCart = productRepository.findAllByIdIn(cart.items.map { it.productId })

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
                product.stock!! < cartItem.quantity -> validationErrors[cartItem.productId] = "상품의 재고 수량이 부족합니다."
                else -> {
                    product.stock = product.stock!! - cartItem.quantity
                    purchaseProducts.add(PurchaseProduct(purchase, product.id!!, cartItem.quantity))
                }
            }
        }

        throwPurchaseByCartException(validationErrors)
        return purchaseProducts
    }

    private fun throwPurchaseByCartException(validationErrors: Map<Long, String>) {
        if (validationErrors.isNotEmpty()) {
            throw Exception("장바구니 구매 오류.")
        }
    }

}
