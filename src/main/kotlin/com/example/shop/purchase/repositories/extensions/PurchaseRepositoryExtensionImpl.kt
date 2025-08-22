package com.example.shop.purchase.repositories.extensions

import com.example.shop.purchase.domain.Purchase
import com.example.shop.purchase.domain.QPurchase
import com.example.shop.purchase.domain.QPurchaseProduct
import com.example.shop.refund.domain.QRefund
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
class PurchaseRepositoryExtensionImpl : QuerydslRepositorySupport(Purchase::class.java), PurchaseRepositoryExtension {
    override fun searachAccountPurchase(
        purchaseId: Long,
        accountId: Long,
    ): Purchase? {
        val purchase = QPurchase.purchase
        val refund = QRefund.refund

        return from(purchase)
            .leftJoin(purchase.refund, refund).fetchJoin()
            .where(purchase.id.eq(purchaseId))
            .where(purchase.account.id.eq(accountId))
            .fetchOne()
    }

    override fun searchWithPurchaseProduct(
        purchaseIds: List<Long>?,
        accountId: Long,
        pageable: Pageable,
    ): Page<Purchase> {
        val purchase = QPurchase.purchase
        val purchaseProduct = QPurchaseProduct.purchaseProduct
        val refund = QRefund.refund

        val query = from(purchase)
            .leftJoin(purchase.refund, refund).fetchJoin()
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
