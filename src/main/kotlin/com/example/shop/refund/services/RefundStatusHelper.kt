package com.example.shop.refund.services

import com.example.shop.common.apis.exceptions.BadRequestException
import com.example.shop.refund.domain.Refund
import com.example.shop.refund.enums.RefundStatus
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

@Component
class RefundStatusHelper {

    fun setRefundToRequested(refundToRequest: Refund): Refund {
        return when(refundToRequest.status) {
            RefundStatus.REQUESTED -> { refundToRequest }
            RefundStatus.REFUNDED -> { throw BadRequestException("The refund request is already refunded.") }
            RefundStatus.DENIED -> { throw BadRequestException("The refund is denied. reason: ${refundToRequest.etc}") }
            RefundStatus.CANCELED -> {
                refundToRequest.status = RefundStatus.REQUESTED
                refundToRequest.updatedAt = OffsetDateTime.now()
                refundToRequest
            }
        }
    }

    fun setRefundToCanceled(refundToCancel: Refund): Refund {
        return when(refundToCancel.status) {
            RefundStatus.CANCELED -> { refundToCancel }
            RefundStatus.REFUNDED, RefundStatus.DENIED -> {
                throw BadRequestException("The refund can't be canceled. it is already processed")
            }
            RefundStatus.REQUESTED -> {
                refundToCancel.status = RefundStatus.CANCELED
                refundToCancel.updatedAt = OffsetDateTime.now()
                refundToCancel
            }
        }
    }
}
