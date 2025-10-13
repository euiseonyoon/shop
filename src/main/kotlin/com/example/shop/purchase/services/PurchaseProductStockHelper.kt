package com.example.shop.purchase.services

import com.example.shop.kafka.KafkaMessageSender
import com.example.shop.kafka.product_stock_topic.models.ProductStockUpdateKafkaMessage
import com.example.shop.purchase.domain.PurchaseProduct
import com.example.shop.purchase.enums.PurchaseProductStatus
import org.springframework.stereotype.Component

@Component
class PurchaseProductStockHelper(
    private val kafkaMessageSender: KafkaMessageSender,
) {
    fun restorePurchasedProductStock(purchaseProducts: List<PurchaseProduct>) {
        purchaseProducts
            .filter { it.status != PurchaseProductStatus.PRODUCT_STOCK_RESTORED }
            .forEach {
                sendStockRestoreMessage(it.productId, it.id, it.count)
            }
    }

    fun sendStockDecrementMessage(productId: Long, purchaseProductId: Long, quantity: Int) {
        require(quantity > 0)
        sendMessage(productId, purchaseProductId, -1 * quantity)
    }

    fun sendStockRestoreMessage(productId: Long, purchaseProductId: Long, quantity: Int) {
        require(quantity > 0)
        sendMessage(productId, purchaseProductId, quantity)
    }

    private fun sendMessage(productId: Long, purchaseProductId: Long, purchasedQuantity: Int) {
        val msg = ProductStockUpdateKafkaMessage(purchaseProductId, productId, purchasedQuantity)
        kafkaMessageSender.sendProductStockUpdateMessage(msg)
    }
}
