package com.example.shop.kafka.product_stock_topic.models

import kotlinx.serialization.Serializable

@Serializable
data class ProductStockUpdateKafkaMessage(
    val purchaseProductId: Long?,
    val productId: Long,
    val updateAmount: Int, // 음수 혹은 양수, 음수일때는 재고 차감, 양수일때는 재고 추가
) {
    init {
        require(updateAmount != 0)
    }
}

