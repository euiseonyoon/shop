package com.example.shop.purchase.services

import com.example.shop.purchase.domain.Purchase
import com.example.shop.purchase.enums.PurchaseStatus
import com.example.shop.purchase.models.PurchaseApproveRequest
import com.example.shop.purchase.models.PurchaseApproveResult
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class PurchaseApproveHelperImpl(
    private val purchaseHelper: PurchaseHelper,
    private val tossPaymentService: TossPaymentService,
) : PurchaseApproveHelper {

    override val maxStockUpdatedTrial = 3
    override val stockUpdatedCheckIntervalMilliSeconds = 200L

    @Transactional
    override fun approveByPurchaseStatus(purchase: Purchase, request: PurchaseApproveRequest): PurchaseApproveResult {
        when(purchase.status) {
            PurchaseStatus.STOCK_INSUFFICIENT -> {
                purchaseHelper.restorePurchaseProductsStock(purchase.id)
                return PurchaseApproveResult(false, "해당 구매에 포함된 상품 중, 재고가 부족한 상품으로 인해 진행될 수 없습니다.")
            }
            PurchaseStatus.APPROVED -> {
                return PurchaseApproveResult(false, "이미 승인된 구매입니다.")
            }
            PurchaseStatus.FAILED -> {
                return PurchaseApproveResult(false, "실패 처리된 구매입니다.")
            }
            PurchaseStatus.STOCK_NOT_UPDATED_IN_TIME -> {
                return PurchaseApproveResult(false, "시간 내에 처리 되지 못한 구매입니다.")
            }
            PurchaseStatus.PURCHASED_TOTAL_PRICE_DIFFERENT -> {
                return PurchaseApproveResult(false, "구매 총 금액이 상이해서 취소된 구매입니다.")
            }
            PurchaseStatus.READY -> {
                checkPurchasePrice(purchase, request.amount).let { (isOk, failedReason) ->
                    if (!isOk) {
                        purchaseHelper.handlePurchaseIfFails(purchase, PurchaseStatus.PURCHASED_TOTAL_PRICE_DIFFERENT)
                        return PurchaseApproveResult(false, failedReason)
                    }
                }
                checkPurchaseProductStockUpdated(purchase.id).let { (isOk, failedReason) ->
                    if (!isOk) {
                        purchaseHelper.handlePurchaseIfFails(purchase, PurchaseStatus.STOCK_NOT_UPDATED_IN_TIME)
                        return PurchaseApproveResult(false, failedReason)
                    }
                }

                return sendApproveRequest(request).also {
                    if (!it.isApproved) {
                        purchaseHelper.handlePurchaseIfFails(purchase, PurchaseStatus.FAILED)
                    }
                }
            }
        }
    }

    private fun sendApproveRequest(request: PurchaseApproveRequest): PurchaseApproveResult {
        return try {
            tossPaymentService.sendPaymentApproveRequest(request)
            PurchaseApproveResult(true, null)
        } catch (e: Exception) {
            PurchaseApproveResult(false, "${e.message}")
        }
    }

    private fun checkPurchasePrice(purchase: Purchase, totalApprovingPrice: Int): Pair<Boolean, String?>{
        if (totalApprovingPrice != purchase.totalPrice) {
            return false to "구매 가격이 서로 다릅니다."
        }
        return true to null
    }

    private fun checkPurchaseProductStockUpdated(purchaseId: Long): Pair<Boolean, String?>{
        if (!purchaseHelper.checkIfStocksUpdated(purchaseId, maxStockUpdatedTrial, stockUpdatedCheckIntervalMilliSeconds)) {
            false to "시간 내에 구매상품의 재고 차감이 이루어 지지 않았습니다."
        }

        return true to null
    }
}
