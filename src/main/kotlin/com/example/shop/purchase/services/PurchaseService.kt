package com.example.shop.purchase.services

import com.example.shop.cart.services.CartDomainService
import com.example.shop.kafka.KafkaMessageSender
import com.example.shop.kafka.product_stock_topic.models.ProductStockUpdateKafkaMessage
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
    private val kafkaMessageSender: KafkaMessageSender,
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

    private fun sendProductStockUpdateKafkaMessage(productId: Long, purchaseProductId: Long, purchasedQuantity: Int) {
        require(purchasedQuantity > 0)
        val msg = ProductStockUpdateKafkaMessage(purchaseProductId, productId, -1 * purchasedQuantity)
        kafkaMessageSender.sendProductStockUpdateMessage(msg)
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

        sendProductStockUpdateKafkaMessage(product.id, savedPurchaseProduct.id, request.quantity)

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
            val savedPurchasedProduct = purchaseProductRepository.save(PurchaseProduct(purchase, product.id, quantity))

            sendProductStockUpdateKafkaMessage(product.id, savedPurchasedProduct.id, quantity)

            savedPurchasedProduct
        }

        cartDomain.cart.isPurchased = true // 장바구니 구매 상태 업데이트

        return PurchaseDomain(purchase, purchaseProducts)
    }
}
