package com.example.shop.kafka.notify_topic.models

import com.example.shop.kafka.notify_topic.enums.NotifyType
import kotlinx.serialization.Serializable

@Serializable
data class NotifyKafkaMessage(
    val type: NotifyType,
    val content: NotifyKafkaContent,
) {
    init {
        when (type) {
            NotifyType.NOTIFY_USER_REFUND, NotifyType.NOTIFY_ADMIN_REFUND -> {
                require(content is NotifyKafkaContent.RefundKafkaDto)
            }
            NotifyType.AUTO_REGISTERED_ACCOUNT -> {
                require(content is NotifyKafkaContent.AutoRegisteredAccountKafkaDto)
            }
        }
    }
}
