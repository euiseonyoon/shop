package com.example.shop.refund.models

import com.example.shop.common.apis.models.AccountDto
import com.example.shop.refund.enums.RefundStatus
import kotlinx.serialization.Serializable

@Serializable
data class RefundKafkaDto(
    val refundId: Long,
    val refundStatus: RefundStatus,
    val accountInfo: AccountDto,
)
