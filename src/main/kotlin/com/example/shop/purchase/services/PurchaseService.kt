package com.example.shop.purchase.services

import com.example.shop.auth.models.AccountAuthenticationToken
import com.example.shop.cart.services.CartService
import com.example.shop.products.services.ProductService
import com.example.shop.purchase.domain.Purchase
import com.example.shop.purchase.domain.PurchaseProduct
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
    private val purchaseHelper: PurchaseHelper,
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

        val product = purchaseHelper.filterProductOrThrow(
            product = productService.findById(request.productId),
            quantity = request.quantity,
        )

        product.decrementStock(request.quantity)
        purchase.addPurchaseProduct(PurchaseProduct(purchase, product.id, request.quantity))

        return purchaseRepository.save(purchase)
    }

    @Transactional
    fun purchaseByCart(accountId: Long): Purchase? {
        val cart = cartService.getMyCart(accountId) ?: return null
        val productsInCart = productService.findByIds(cart.items.map { it.productId })

        val purchase = Purchase(cart.accountId)
        val purchasableProducts = purchaseHelper.filterProductsOrThrow(cart.items, productsInCart)
        val purchaseProducts = purchaseHelper.processPurchasing(purchase, purchasableProducts)

        purchase.addPurchaseProducts(purchaseProducts)
        cart.isPurchased = true // 장바구니 구매 상태 업데이트

        return purchaseRepository.save(purchase)
    }
}
