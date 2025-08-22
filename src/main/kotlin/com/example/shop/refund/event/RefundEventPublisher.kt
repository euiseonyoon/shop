package com.example.shop.refund.event

import com.example.shop.refund.domain.Refund

interface RefundEventPublisher {
    fun notifyAdminRefundRequested(refund: Refund)

    fun notifyUserRefundResult(refund: Refund)
}
