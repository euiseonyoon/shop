
package com.example.shop.refund.models

import com.example.shop.refund.enums.RefundStatus

data class AdminUpdateRefundRequest(
    val refundId: Long,
    val status: RefundStatus,
    val etc: String?
)
