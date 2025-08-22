package com.example.shop.refund.event

import com.example.shop.refund.domain.Refund
import com.example.shop.refund.event.models.RefundEventForAdmin
import com.example.shop.refund.event.models.RefundEventForUser
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class RefundEventPublisherImpl(
    private val eventPublisher: ApplicationEventPublisher
): RefundEventPublisher {
    override fun notifyAdminRefundRequested(refund: Refund) {
        val event = RefundEventForAdmin(refund)
        eventPublisher.publishEvent(event)
    }

    override fun notifyUserRefundResult(refund: Refund) {
        val event = RefundEventForUser(refund)
        eventPublisher.publishEvent(event)
    }
}

