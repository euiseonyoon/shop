package com.example.shop.kafka.product_stock_topic.message_handlers

import com.example.shop.kafka.KafkaMessageHandler
import com.example.shop.kafka.product_stock_topic.models.ProductStockUpdateKafkaMessage
import com.example.shop.products.respositories.ProductRepository
import com.example.shop.purchase.enums.PurchaseProductStatus
import com.example.shop.purchase.repositories.PurchaseProductRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ProductStockUpdateKafkaMsgHandler(
    private val productRepository: ProductRepository,
    private val purchaseProductRepository: PurchaseProductRepository
) : KafkaMessageHandler<ProductStockUpdateKafkaMessage> {
    @Transactional
    override fun handleMessage(message: ProductStockUpdateKafkaMessage) {
        val targetProduct = productRepository.findById(message.productId).orElse(null) ?: return

        if (message.updateAmount < 0) {
            targetProduct.decrementStock(-1 * message.updateAmount)
        } else {
            targetProduct.incrementStock(message.updateAmount)
        }
        productRepository.save(targetProduct)

        if (message.purchaseProductId != null) {
            val targetPurchaseProduct = purchaseProductRepository.findById(message.purchaseProductId).orElse(null) ?: return

            if (message.updateAmount < 0) {
                targetPurchaseProduct.status = PurchaseProductStatus.PRODUCT_STOCK_DECREMENTED
            } else {
                targetPurchaseProduct.status = PurchaseProductStatus.PRODUCT_STOCK_RESTORED
            }
            purchaseProductRepository.save(targetPurchaseProduct)
        }
    }
}
