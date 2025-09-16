package com.example.shop.refund.services

import com.example.shop.auth.models.AccountAuthenticationToken
import com.example.shop.common.apis.exceptions.BadRequestException
import com.example.shop.purchase.repositories.PurchaseRepository
import com.example.shop.refund.domain.Refund
import com.example.shop.refund.enums.RefundStatus
import com.example.shop.refund.kafka.RefundKafkaSender
import com.example.shop.refund.models.AdminUpdateRefundRequest
import com.example.shop.refund.models.RefundCancelRequest
import com.example.shop.refund.models.RefundRequest
import com.example.shop.refund.repositories.RefundRepository
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
class RefundService(
    private val refundRepository: RefundRepository,
    private val purchaseRepository: PurchaseRepository,
    private val refundKafkaSender: RefundKafkaSender,
) {
    @Transactional
    fun requestRefund(
        request: RefundRequest,
        accountId: Long
    ): Refund {
        val purchase = purchaseRepository.searachAccountPurchase(request.purchaseId, accountId) ?:
            throw BadRequestException("Purchase not found.")

        val foundRefund = purchase.refund

        // 해당 purchase에 대한 refund 요청 없었음 -> 새로운 refund 요청 생성
        val refund = if (foundRefund == null) {
            Refund().apply {
                this.purchase = purchase
                this.status = RefundStatus.REQUESTED
                this.reason = request.reason
            }
        } else {
            // 해당 purchase에 대한 refund 요청 있었음 -> refund 상태를 확인하고 refund.Cancel 이었다면 REQUESTED로 변경
            when(foundRefund.status) {
                RefundStatus.REQUESTED -> {
                    // requestRefund() 메서드 즉시 종료
                    return foundRefund
                }
                RefundStatus.REFUNDED -> { throw BadRequestException("The refund request is already refunded.") }
                RefundStatus.DENIED -> { throw BadRequestException("The refund is denied. reason: ${foundRefund.etc}") }
                RefundStatus.CANCELED -> {
                    foundRefund.apply {
                        this.status = RefundStatus.REQUESTED
                        this.updatedAt = OffsetDateTime.now()
                    }
                }
            }
        }

        return refundRepository.save(refund).also {
            refundKafkaSender.notifyAdminRefundRequested(refund)
        }
    }

    @Transactional
    fun cancelRefund(request: RefundCancelRequest, accountId: Long): Refund {
        val purchase = purchaseRepository.searachAccountPurchase(request.purchaseId, accountId) ?:
            throw BadRequestException("Purchase not found.")

        val refundToCancel = purchase.refund ?: throw BadRequestException("There is no refund to cancel.")

        val refund = when(refundToCancel.status) {
            RefundStatus.REQUESTED -> {
                refundToCancel.apply {
                    this.status = RefundStatus.CANCELED
                    this.updatedAt = OffsetDateTime.now()
                }
            }
            RefundStatus.REFUNDED, RefundStatus.DENIED -> {
                throw BadRequestException("The refund can't be canceled. it is already processed")
            }
            RefundStatus.CANCELED -> { return refundToCancel }
        }

        return refundRepository.save(refund).also {
            refundKafkaSender.notifyAdminRefundRequested(refund)
        }
    }

    @Transactional
    fun updateRefundStatusAsAdmin(
        request: AdminUpdateRefundRequest,
    ): Refund {
        val refund = refundRepository.findById(request.refundId).orElseThrow {
            throw BadRequestException("Refund not found.")
        }
        if (refund.status == request.status) {
            return if (refund.etc != null) {
                refund.etc = request.etc
                refundRepository.save(refund)
            } else {
                refund
            }
        }

        refund.status = request.status
        refund.etc = refund.etc
        return refundRepository.save(refund).also {
            refundKafkaSender.notifyUserRefundResult(refund)
        }
    }
}
