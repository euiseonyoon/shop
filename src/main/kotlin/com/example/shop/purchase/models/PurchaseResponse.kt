package com.example.shop.purchase.models

import com.example.shop.purchase.domain.PurchaseDomain
import kotlinx.serialization.Serializable
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl

@Serializable
data class PurchaseResponse(
    val purchaseId: Long,
    val purchasedProductInfos: List<PurchasedProductInfo>,
    val purchasedDate: String,
) {
    companion object {
        fun fromPurchaseDomainPage(domainPage: Page<PurchaseDomain>): Page<PurchaseResponse> {
            val content = domainPage.content.map {
                fromPurchaseDomain(it)
            }
            return PageImpl(content, domainPage.pageable, domainPage.totalElements)
        }

        fun fromPurchaseDomain(domain: PurchaseDomain): PurchaseResponse {
            val productInfos = domain.purchaseProducts.map { purchasedProduct ->
                PurchasedProductInfo(
                    productId = purchasedProduct.productId,
                    count = purchasedProduct.count,
                )
            }
            return PurchaseResponse(
                purchaseId = domain.purchase.id,
                purchasedDate = domain.purchase.createdAt.toString(),
                purchasedProductInfos = productInfos,
            )
        }
    }
}
