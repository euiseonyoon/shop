package com.example.shop.refund.services

import com.example.shop.common.apis.exceptions.BadRequestException
import com.example.shop.refund.domain.Refund
import com.example.shop.refund.enums.RefundStatus
import com.example.shop.refund.kafka.RefundKafkaSender
import com.example.shop.refund.models.AdminUpdateRefundRequest
import com.example.shop.refund.models.RefundCancelRequest
import com.example.shop.refund.models.RefundRequest
import com.example.shop.refund.repositories.RefundRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Component
class RefundService(
    private val refundRepository: RefundRepository,
    private val refundKafkaSender: RefundKafkaSender,
) {
    @Transactional
    fun requestRefund(
        request: RefundRequest,
        accountEmail: String,
    ): Refund {
        val foundRefund = refundRepository.findByPurchaseId(request.purchaseId)

        val refund = foundRefund?.let { makeRefundRequest(it) } ?:
            Refund(purchaseId = request.purchaseId, reason = request.reason)

        return refundRepository.save(refund).also {
            refundKafkaSender.notifyAdminRefundRequested(accountEmail, it)
        }
    }

    private fun makeRefundRequest(foundRefund: Refund): Refund {
        when(foundRefund.status) {
            RefundStatus.REQUESTED -> { }
            RefundStatus.REFUNDED -> { throw BadRequestException("The refund request is already refunded.") }
            RefundStatus.DENIED -> { throw BadRequestException("The refund is denied. reason: ${foundRefund.etc}") }
            RefundStatus.CANCELED -> {
                foundRefund.status = RefundStatus.REQUESTED
                foundRefund.updatedAt = OffsetDateTime.now()
            }
        }
        return foundRefund
    }

    @Transactional
    fun cancelRefund(
        request: RefundCancelRequest,
        accountEmail: String,
    ): Refund {
        val refundToCancel = refundRepository.findByPurchaseId(request.purchaseId)
            ?: throw BadRequestException("There is no refund to cancel.")

        val refund = setCancelStatus(refundToCancel)

        return refundRepository.save(refund).also {
            refundKafkaSender.notifyAdminRefundRequested(accountEmail, it)
        }
    }

    private fun setCancelStatus(refundToCancel: Refund): Refund {
        return when(refundToCancel.status) {
            RefundStatus.CANCELED -> { refundToCancel }
            RefundStatus.REQUESTED -> {
                refundToCancel.status = RefundStatus.CANCELED
                refundToCancel.updatedAt = OffsetDateTime.now()
                refundToCancel
            }
            RefundStatus.REFUNDED, RefundStatus.DENIED -> {
                throw BadRequestException("The refund can't be canceled. it is already processed")
            }
        }
    }

    @Transactional
    fun updateRefundStatusAsAdmin(
        request: AdminUpdateRefundRequest,
        accountEmail: String,
    ): Refund {
        val refund = refundRepository.findById(request.refundId).orElseThrow {
            throw BadRequestException("Refund not found.")
        }
        if (refund.status == request.status) {
            return request.etc?.let { refundRepository.save(refund.apply { this.etc = it }) } ?: refund
        }

        refund.status = request.status
        refund.etc = refund.etc

        return refundRepository.save(refund).also {
            refundKafkaSender.notifyUserRefundResult(accountEmail, it)
        }
    }
}
