package com.example.shop.purchase.repositories.extensions

import com.example.shop.purchase.domain.Purchase
import com.example.shop.purchase.domain.QPurchase
import com.example.shop.purchase.domain.QPurchaseProduct
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
class PurchaseRepositoryExtensionImpl : QuerydslRepositorySupport(Purchase::class.java), PurchaseRepositoryExtension {
    override fun searchWithPurchaseProduct(
        purchaseIds: List<Long>?,
        accountId: Long,
        pageable: Pageable,
    ): Page<Purchase> {
        val purchase = QPurchase.purchase
        val purchaseProduct = QPurchaseProduct.purchaseProduct

        val query = from(purchase)
            .join(purchase.purchaseProducts, purchaseProduct).fetchJoin()
            .where(purchase.account.id.eq(accountId))

        if (!purchaseIds.isNullOrEmpty()) {
            query.where(purchase.id.`in`(purchaseIds))
        }

        val totalCount = query.fetchCount()
        val pagedQuery = getQuerydsl()!!.applyPagination(pageable, query)
        val content = pagedQuery.fetch()

        return PageImpl(content, pageable, totalCount)
    }
}
