package com.example.shop.refund.services

import com.example.shop.common.apis.exceptions.BadRequestException
import com.example.shop.common.apis.exceptions.NotFoundException
import com.example.shop.refund.domain.Refund
import com.example.shop.refund.kafka.RefundKafkaSender
import com.example.shop.refund.models.AdminUpdateRefundRequest
import com.example.shop.refund.models.RefundCancelRequest
import com.example.shop.refund.models.RefundRequest
import com.example.shop.refund.repositories.RefundRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class RefundService(
    private val refundRepository: RefundRepository,
    private val refundKafkaSender: RefundKafkaSender,
    private val refundStatusHelper: RefundStatusHelper
) {
    @Transactional
    fun requestRefund(
        request: RefundRequest,
        accountEmail: String,
    ): Refund {
        val foundRefund = refundRepository.findByPurchaseId(request.purchaseId)

        val refund = foundRefund?.let { refundStatusHelper.setRefundToRequested(it) } ?:
            Refund(purchaseId = request.purchaseId, reason = request.reason)

        return refundRepository.save(refund).also {
            refundKafkaSender.notifyAdminRefundRequested(accountEmail, it)
        }
    }

    @Transactional
    fun cancelRefund(
        request: RefundCancelRequest,
        accountEmail: String,
    ): Refund {
        val refundToCancel = refundRepository.findByPurchaseId(request.purchaseId)
            ?: throw BadRequestException("There is no refund to cancel.")

        val refund = refundStatusHelper.setRefundToCanceled(refundToCancel)

        return refundRepository.save(refund).also {
            refundKafkaSender.notifyAdminRefundRequested(accountEmail, it)
        }
    }

    @Transactional
    fun updateRefundStatusAsAdmin(
        request: AdminUpdateRefundRequest,
        accountEmail: String,
    ): Refund {
        val refund = refundRepository.findById(request.refundId).orElseThrow {
            throw NotFoundException("Refund not found.")
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
