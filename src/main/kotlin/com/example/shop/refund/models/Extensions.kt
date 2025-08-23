package com.example.shop.refund.models

import com.example.shop.refund.domain.Refund

fun Refund.toRequestDto(): RefundRequestDto {
    return RefundRequestDto(
        purchaseId = this.purchase!!.id!!,
        status = this.status.name,
        createdAt = this.createdAt.toString(),
        updatedAt = this.updatedAt?.toString(),
        etc = this.etc
    )
}
