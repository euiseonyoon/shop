package com.example.shop.refund.services

import com.example.shop.auth.models.AccountAuthenticationToken
import com.example.shop.common.apis.exceptions.BadRequestException
import com.example.shop.purchase.repositories.PurchaseRepository
import com.example.shop.refund.domain.Refund
import com.example.shop.refund.enums.RefundStatus
import com.example.shop.refund.event.RefundEventPublisher
import com.example.shop.refund.models.RefundRequest
import com.example.shop.refund.repositories.RefundRepository
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RefundService(
    private val refundRepository: RefundRepository,
    private val purchaseRepository: PurchaseRepository,
    private val refundEventPublisher: RefundEventPublisher,
) {
    @Transactional
    fun requestRefund(
        request: RefundRequest,
        authentication: Authentication
    ): Refund {
        val auth = authentication as AccountAuthenticationToken
        val purchase = purchaseRepository.searachAccountPurchase(request.purchaseId, auth.accountId) ?:
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
                    foundRefund.apply { this.status = RefundStatus.REQUESTED }
                }
            }
        }

        return refundRepository.save(refund).also {
            refundEventPublisher.notifyAdminRefundRequested(refund)
        }
    }
}
