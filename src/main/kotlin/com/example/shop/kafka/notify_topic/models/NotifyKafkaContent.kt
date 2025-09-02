package com.example.shop.kafka.notify_topic.models

import com.example.shop.common.apis.models.AccountDto
import com.example.shop.refund.enums.RefundStatus
import kotlinx.serialization.Serializable


@Serializable
sealed class NotifyKafkaContent {
    @Serializable
    data class AutoRegisteredAccountKafkaDto(
        val email: String,
        val rawPassword: String,
    ) : NotifyKafkaContent()

    @Serializable
    data class RefundKafkaDto(
        val refundId: Long,
        val refundStatus: RefundStatus,
        val accountInfo: AccountDto,
    ) : NotifyKafkaContent()
}
