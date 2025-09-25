package com.example.shop.purchase.services

import com.example.shop.auth.models.AccountAuthenticationToken
import com.example.shop.auth.repositories.AccountRepository
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

    fun checkProductsBeforePurchase(cartItems: List<CartItem>, products: List<Product>): Map<Long, Product>{
        val productIdMap: Map<Long, Product> = products.associateBy { it.id!! }

        val notAvailableProducts = cartItems.mapNotNull { cartItem ->
            val product = productIdMap[cartItem.productId]
            when {
                product == null -> Pair(cartItem.productId, "상품을 찾을 수 없습니다.")
                product.stock!! < cartItem.quantity -> Pair(cartItem.productId, "상품의 재고 수량이 부족합니다.")
                else -> null
            }
        }
        if (notAvailableProducts.isNotEmpty()) {
            throw Exception("장바구니 구매 오류.")
        }

        return productIdMap
    }

    @Transactional
    fun purchaseByCart(accountId: Long): Purchase? {
        val cart = cartService.getMyCart(accountId) ?: return null
        val productsInCart = productRepository.findAllByIdIn(cart.items.map { it.productId })

        val productIdMap = checkProductsBeforePurchase(cart.items.toList(), productsInCart)

        val purchaseProducts = cart.items.map { cartItem ->
            val product = productIdMap[cartItem.productId]!!
            product.stock = product.stock!! - cartItem.quantity
            PurchaseProduct.create(product, cartItem.quantity)
        }

        val purchase = Purchase().apply {
            // TODO: 추후 Purchase.account -> Purchase.accountId로 수정한다.
            this.account = accountRepository.findById(cart.accountId).get()
        }
        purchase.addPurchaseProducts(purchaseProducts)

        cart.isPurchased = true

        return purchaseRepository.save(purchase)
    }

}
