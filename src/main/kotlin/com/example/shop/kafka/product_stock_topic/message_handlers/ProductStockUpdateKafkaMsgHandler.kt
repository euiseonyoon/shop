package com.example.shop.kafka.product_stock_topic.message_handlers

import com.example.shop.cart.respositories.CartRepository
import com.example.shop.kafka.KafkaMessageHandler
import com.example.shop.kafka.product_stock_topic.models.ProductStockUpdateKafkaMessage
import com.example.shop.products.domain.Product
import com.example.shop.products.exceptions.ProductInsufficientStockException
import com.example.shop.products.respositories.ProductRepository
import com.example.shop.purchase.domain.Purchase
import com.example.shop.purchase.domain.PurchaseProduct
import com.example.shop.purchase.enums.PurchaseProductStatus
import com.example.shop.purchase.enums.PurchaseStatus
import com.example.shop.purchase.repositories.PurchaseProductRepository
import com.example.shop.purchase.repositories.PurchaseRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ProductStockUpdateKafkaMsgHandler(
    private val productRepository: ProductRepository,
    private val purchaseProductRepository: PurchaseProductRepository,
    private val purchaseRepository: PurchaseRepository,
    private val cartRepository: CartRepository,
) : KafkaMessageHandler<ProductStockUpdateKafkaMessage> {
    @Transactional
    override fun handleMessage(message: ProductStockUpdateKafkaMessage) {
        val targetProduct = productRepository.findById(message.productId).orElse(null) ?: return
        // 단순 상품의 재고 업데이트만
        if (message.purchaseProductId == null) {
            updateProductStock(message.updateAmount, targetProduct)
            return
        }

        // 구매(Purchase)에 포함된 상품의 재고를 차감하거나 차감된 재고를 원복시킴.
        val targetPurchaseProduct = purchaseProductRepository.findByIdWithPurchase(message.purchaseProductId) ?: return
        try {
            // 구매된 상품 재고 차감 혹은 구매 처리된 상품 재고 원복
            decrementOrRestoreProductStock(message.updateAmount, targetProduct, targetPurchaseProduct)
        } catch (e: ProductInsufficientStockException) {
            // 상품의 구매 처리중, 재고 부족으로 오류 발생

            // 1. 현재 purchaseProduct에 구매를 원복했다고 표시한다. (재고 차감을 원상복귀 시켰다고 표시한다).
            targetPurchaseProduct.status = PurchaseProductStatus.PRODUCT_STOCK_RESTORED
            purchaseProductRepository.save(targetPurchaseProduct)

            // 2. Purchase를 구매 진행이 될 수 없는 상태(INVALID)로 표시한다.
            setPurchaseInvalid(targetPurchaseProduct.purchase)
        }
    }

    private fun setPurchaseInvalid(purchase: Purchase) {
        // 1. Purchase의 상태를 INVALID 라고 저장한다.
        purchase.apply { this.status = PurchaseStatus.INVALID }.let { purchaseRepository.save(it) }

        // 2. Purchase가 장바구니(cart)를 통해 이루어 진것이라면 Cart의 구매완료 정보도 수정한다.
        purchase.cartId ?.let { cartId ->
            cartRepository.findById(cartId).orElse(null)?.apply {
                this.isPurchased = false
            }?.let { cartRepository.save(it) }
        }
    }

    private fun updateProductStock(updateAmount: Int, product: Product) {
        if (updateAmount < 0) {
            product.decrementStock(-1 * updateAmount)
        } else {
            product.incrementStock(updateAmount)
        }
        productRepository.save(product)
    }

    private fun decrementOrRestoreProductStock(
        updateAmount: Int,
        product: Product,
        targetPurchaseProduct: PurchaseProduct,
    ) {
        val status = when {
            updateAmount < 0 -> {
                require(targetPurchaseProduct.status == PurchaseProductStatus.READY) {
                    "구매하려는 상품의 재고를 차감하기 위해서는 READY 상태이어야 합니다. ${targetPurchaseProduct.id}"
                }
                PurchaseProductStatus.PRODUCT_STOCK_DECREMENTED
            }
            updateAmount > 0 -> {
                require(targetPurchaseProduct.status == PurchaseProductStatus.PRODUCT_STOCK_DECREMENTED) {
                    "구매된 상품의 재고를 원복하기 위해서는 PRODUCT_STOCK_DECREMENTED 상태이어야 합니다. ${targetPurchaseProduct.id}"
                }
                PurchaseProductStatus.PRODUCT_STOCK_RESTORED
            }
            else -> return
        }

        updateProductStock(updateAmount, product)

        targetPurchaseProduct.status = status
        purchaseProductRepository.save(targetPurchaseProduct)
    }
}
