package com.example.shop.purchase.services

import com.example.shop.purchase.domain.Purchase
import com.example.shop.purchase.enums.PurchaseStatus
import com.example.shop.purchase.models.PurchaseApproveRequest
import com.example.shop.purchase.models.PurchaseApproveResult
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class PurchaseApproveHelper(
    private val purchaseHelper: PurchaseHelper,
    private val tossPaymentService: TossPaymentService,
) {
    val maxStockUpdatedTrial = 3
    val stockUpdatedCheckIntervalMilliSeconds = 200L

    @Transactional
    fun approveByPurchaseStatus(purchase: Purchase, request: PurchaseApproveRequest): PurchaseApproveResult {
        when(purchase.status) {
            PurchaseStatus.STOCK_INSUFFICIENT -> {
                purchaseHelper.restorePurchaseProductsStock(purchase.id)
                return PurchaseApproveResult(false, "해당 구매에 포함된 상품 중, 재고가 부족한 상품으로 인해 진행될 수 없습니다.")
            }
            PurchaseStatus.APPROVED -> {
                return PurchaseApproveResult(false, "이미 승인된 구매입니다.")
            }
            PurchaseStatus.FAILED -> {
                return PurchaseApproveResult(false, "결제 승인 오류로 인해 실패 처리된 구매입니다.")
            }
            PurchaseStatus.STOCK_NOT_UPDATED_IN_TIME -> {
                return PurchaseApproveResult(false, "시간 내에 처리 되지 못한 구매입니다.")
            }
            PurchaseStatus.PURCHASED_TOTAL_PRICE_DIFFERENT -> {
                return PurchaseApproveResult(false, "구매 총 금액이 상이해서 취소된 구매입니다.")
            }
            PurchaseStatus.READY -> {
                validatePurchaseBeforeApprove(purchase, request.amount)?.let { (failedReason, failStatus) ->
                    purchaseHelper.handlePurchaseIfFails(purchase, failStatus)
                    return PurchaseApproveResult(false, failedReason)
                }

                return sendApproveRequest(request).also {
                    if (!it.isApproved) {
                        purchaseHelper.handlePurchaseIfFails(purchase, PurchaseStatus.FAILED)
                    }
                }
            }
        }
    }

    fun validatePurchaseBeforeApprove(purchase: Purchase, approvingTotal: Int): Pair<String, PurchaseStatus>? {
        if (approvingTotal != purchase.totalPrice) {
            return "결제된 금액과 구매된 총 금액이 상이합니다." to PurchaseStatus.PURCHASED_TOTAL_PRICE_DIFFERENT
        }

        purchaseHelper.checkIfStocksUpdated(
            purchase.id,
            maxStockUpdatedTrial,
            stockUpdatedCheckIntervalMilliSeconds,
        ).let {
            if (!it) return "재품 재고를 감소 시간초과로 구매가 취소 되었습니다." to PurchaseStatus.STOCK_NOT_UPDATED_IN_TIME
        }

        return null
    }

    fun sendApproveRequest(request: PurchaseApproveRequest): PurchaseApproveResult {
        return try {
            tossPaymentService.sendPaymentApproveRequest(request)
            PurchaseApproveResult(true, null)
        } catch (e: Exception) {
            PurchaseApproveResult(false, "토스 결제 승인 오류. ${e.message}")
        }
    }
}
