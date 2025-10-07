package com.example.shop.kafka.product_stock_topic.models

import kotlinx.serialization.Serializable

@Serializable
data class ProductStockUpdateKafkaMessage(
    val purchaseProductId: Long?,
    val productId: Long,
    val updateAmount: Int, // 음수 혹은 양수
) {
    init {
        require(updateAmount != 0)
    }
}

