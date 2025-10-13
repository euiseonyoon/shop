package com.example.shop.purchase.services

import com.example.shop.purchase.domain.Purchase
import com.example.shop.purchase.enums.PurchaseStatus
import com.example.shop.purchase.models.PurchaseApproveRequest
import com.example.shop.purchase.models.PurchaseApproveResult
import com.example.shop.purchase.repositories.PurchaseProductRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class PurchaseApproveHelperImpl(
    private val purchaseProductRepository: PurchaseProductRepository,
    private val purchaseProductService: PurchaseProductService,
    private val purchaseProductStockHelper: PurchaseProductStockHelper,
    private val purchaseHelper: PurchaseHelper,
) : PurchaseApproveHelper {
    override val maxStockUpdatedTrial = 3
    override val stockUpdatedCheckIntervalMilliSeconds = 200L

    @Transactional
    override fun approveByPurchaseStatus(purchase: Purchase, request: PurchaseApproveRequest): PurchaseApproveResult {
        when(purchase.status) {
            PurchaseStatus.STOCK_INSUFFICIENT -> {
                restorePurchaseProductsStock(purchase.id)
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
            PurchaseStatus.READY -> {
                val (isOk, reason) = checkPurchase(purchase, request.amount)
                if (!isOk) {
                    return PurchaseApproveResult(false, reason)
                }

                // TODO: 토스 페이먼츠 /v1/payments/confirm 호출하기
                // https://docs.tosspayments.com/reference#%EA%B2%B0%EC%A0%9C-%EC%8A%B9%EC%9D%B8
                return PurchaseApproveResult(true, null)
            }
        }
    }

    fun restorePurchaseProductsStock(purchaseId: Long) {
        purchaseProductRepository.findByPurchaseId(purchaseId).let {
            purchaseProductStockHelper.restorePurchasedProductStock(it)
        }
    }

    fun checkPurchase(purchase: Purchase, totalApprovingPrice: Int): Pair<Boolean, String?> {
        if (totalApprovingPrice != purchase.totalPrice) {
            return false to "구매 가격이 서로 다릅니다."
        }

        if (!waitForStockUpdated(purchase)) {
            purchaseHelper.updatePurchaseStatus(purchase, PurchaseStatus.STOCK_NOT_UPDATED_IN_TIME)
            restorePurchaseProductsStock(purchase.id)
            false to "시간 내에 구매상품의 재고 차감이 이루어 지지 않았습니다."
        }

        return true to null
    }

    override fun waitForStockUpdated(purchase: Purchase): Boolean {
        for (i in 1..maxStockUpdatedTrial) {
            if (purchaseProductService.isAllStockUpdated(purchase.id)) {
                return true
            }
            Thread.sleep(stockUpdatedCheckIntervalMilliSeconds)
        }
        return false
    }
}
