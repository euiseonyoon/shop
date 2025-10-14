package com.example.shop.purchase.services

import com.example.shop.cart.services.CartDomainService
import com.example.shop.products.services.ProductService
import com.example.shop.purchase.domain.FailedPurchase
import com.example.shop.purchase.domain.Purchase
import com.example.shop.purchase.domain.PurchaseDomain
import com.example.shop.purchase.domain.PurchaseProduct
import com.example.shop.purchase.enums.PurchaseStatus
import com.example.shop.purchase.exceptions.PurchaseNotFoundException
import com.example.shop.purchase.models.PurchaseApproveRequest
import com.example.shop.purchase.models.PurchaseApproveResult
import com.example.shop.purchase.models.PurchaseDirectlyRequest
import com.example.shop.purchase.models.PurchaseFailRequest
import com.example.shop.purchase.repositories.FailedPurchaseRepository
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
    private val purchaseProductStockHelper: PurchaseProductStockHelper,
    private val purchaseApproveHelper: PurchaseApproveHelper,
    private val paymentService: PaymentService,
    private val failedPurchaseRepository: FailedPurchaseRepository,
) {
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
            product = productService.findById(request.productId),
            quantity = request.quantity,
        )

        val savedPurchase = purchaseRepository.save(Purchase(accountId, product.price))
        val savedPurchaseProduct = purchaseProductRepository.save(
            PurchaseProduct(savedPurchase, product.id, request.quantity)
        )

        purchaseProductStockHelper.sendStockDecrementMessage(product.id, savedPurchaseProduct.id, request.quantity)

        return PurchaseDomain(savedPurchase, listOf(savedPurchaseProduct))
    }

    @Transactional
    fun purchaseByCart(accountId: Long): PurchaseDomain? {
        val cartDomain = cartDomainService.getMyCart(accountId) ?: return null
        val productsInCart = productService.findByIds(cartDomain.cartItems.map { it.productId })
        val purchasableProducts = purchaseHelper.filterProductsOrThrow(cartDomain.cartItems, productsInCart)

        val totalPrice = purchasableProducts.sumOf { (product, quantity) -> product.price * quantity }
        val purchase = purchaseRepository.save(Purchase(cartDomain.cart.accountId, totalPrice, cartDomain.cart.id))

        val purchaseProducts = purchasableProducts.map { (product, quantity) ->
            purchaseProductRepository.save(PurchaseProduct(purchase, product.id, quantity)).also {
                purchaseProductStockHelper.sendStockDecrementMessage(product.id, it.id, quantity)
            }
        }

        cartDomain.cart.isPurchased = true // 장바구니 구매 상태 업데이트

        return PurchaseDomain(purchase, purchaseProducts)
    }

    fun approvePurchase(request: PurchaseApproveRequest): PurchaseApproveResult {
        val purchase = purchaseRepository.findByUuid(request.orderId) ?:
            return PurchaseApproveResult(false, "orderId에 해당하는 구매를 찾을 수 없습니다.")

        paymentService.savePayment(purchase.id, request.paymentKey)

        return purchaseApproveHelper.approveByPurchaseStatus(purchase, request)
    }

    @Transactional
    fun failPurchase(request: PurchaseFailRequest) {
        val purchase = purchaseRepository.findByUuid(request.orderId) ?: throw PurchaseNotFoundException(request.orderId)

        failedPurchaseRepository.save(FailedPurchase(purchase.id, request.errorCode, request.errorMessage))

        purchaseHelper.handlePurchaseIfFails(purchase, PurchaseStatus.FAILED)
    }
}
