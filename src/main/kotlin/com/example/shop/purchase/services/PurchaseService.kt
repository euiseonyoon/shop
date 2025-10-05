package com.example.shop.purchase.services

import com.example.shop.cart.services.CartDomainService
import com.example.shop.products.services.ProductService
import com.example.shop.purchase.domain.Purchase
import com.example.shop.purchase.domain.PurchaseDomain
import com.example.shop.purchase.domain.PurchaseProduct
import com.example.shop.purchase.models.PurchaseDirectlyRequest
import com.example.shop.purchase.repositories.PurchaseProductRepository
import com.example.shop.purchase.repositories.PurchaseRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PurchaseService(
    private val purchaseRepository: PurchaseRepository,
    private val purchaseProductRepository: PurchaseProductRepository,
    private val productService: ProductService,
    private val cartDomainService: CartDomainService,
    private val purchaseHelper: PurchaseHelper,
) {
    @Transactional(readOnly = true)
    fun getMyPurchases(
        purchaseIds: List<Long>?,
        accountId: Long,
        pageable: Pageable,
    ): Page<PurchaseDomain> {
        val totalTargetIds = purchaseIds?.toSet()
        val targetPurchaseIds = getTargetPurchaseIds(totalTargetIds, accountId, pageable)
        val totalCount = getTotalCount(totalTargetIds, accountId)
        val purchaseProducts = purchaseProductRepository.findAllByPurchaseIdIn(targetPurchaseIds)

        val domains = purchaseProducts.groupBy { it.purchase }.map { (purchase, purchaseProducts) ->
            PurchaseDomain(purchase, purchaseProducts)
        }
        return PageImpl(domains, pageable, totalCount)
    }

    private fun getTargetPurchaseIds(totalTargetIds: Set<Long>?, accountId: Long, pageable: Pageable): List<Long> {
        val targetPurchases = if (totalTargetIds == null) {
            purchaseRepository.findAllByAccountId(accountId, pageable).content
        } else {
            purchaseRepository.findAllByIdInAndAccountId(totalTargetIds, accountId, pageable).content
        }
        return targetPurchases.map { it.id }
    }

    private fun getTotalCount(totalTargetIds: Set<Long>?, accountId: Long): Long {
        return if (totalTargetIds == null) {
            purchaseRepository.countByAccountId(accountId).toLong()
        } else {
            purchaseRepository.countByIdInAndAccountId(totalTargetIds, accountId).toLong()
        }
    }

    @Transactional
    fun purchaseDirectly(request: PurchaseDirectlyRequest, accountId: Long): PurchaseDomain {
        val product = purchaseHelper.filterProductOrThrow(
            product = productService.findByIdWithLock(request.productId),
            quantity = request.quantity,
        )
        product.decrementStock(request.quantity)

        val savedPurchase = purchaseRepository.save(Purchase(accountId))
        val savedPurchaseProduct = purchaseProductRepository.save(
            PurchaseProduct(savedPurchase, product.id, request.quantity)
        )

        return PurchaseDomain(savedPurchase, listOf(savedPurchaseProduct))
    }

    @Transactional
    fun purchaseByCart(accountId: Long): PurchaseDomain? {
        val cartDomain = cartDomainService.getMyCart(accountId) ?: return null
        val cart = cartDomain.cart
        val productsInCart = productService.findByIdsWithLock(cartDomain.cartItems.map { it.productId })

        val purchase = purchaseRepository.save(Purchase(cart.accountId))
        val purchasableProducts = purchaseHelper.filterProductsOrThrow(cartDomain.cartItems, productsInCart)
        val purchaseProducts = purchasableProducts.map { (product, quantity) ->
            product.decrementStock(quantity)
            purchaseProductRepository.save(PurchaseProduct(purchase, product.id, quantity))
        }

        cart.isPurchased = true // 장바구니 구매 상태 업데이트

        return PurchaseDomain(purchase, purchaseProducts)
    }
}
