package com.example.shop.refund.models

import kotlinx.serialization.Serializable

@Serializable
data class RefundRequestDto(
    val purchaseId: Long,
    val status: String,
    val createdAt: String,
    val updatedAt: String?,
)
