package com.example.shop.purchase.repositories.extensions

import com.example.shop.purchase.domain.Purchase
import com.example.shop.purchase.domain.QPurchase
import com.example.shop.purchase.domain.QPurchaseProduct
import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
class PurchaseRepositoryExtensionImpl(
    private val queryFactory: JPAQueryFactory
) : QuerydslRepositorySupport(Purchase::class.java), PurchaseRepositoryExtension {
    override fun searchWithPurchaseProduct(
        purchaseIds: List<Long>?,
        accountId: Long,
        pageable: Pageable,
    ): Page<Purchase> {
        val purchase = QPurchase.purchase
        val purchaseProduct = QPurchaseProduct.purchaseProduct

        val builder = BooleanBuilder()
        if (!purchaseIds.isNullOrEmpty()) {
            builder.or(purchase.id.`in`(purchaseIds))
        }

        val totalCount = queryFactory.select(purchase.count())
            .from(purchase)
            .where(purchase.accountId.eq(accountId))
            .where(builder)
            .fetchOne() ?: 0L

        val pagedQuery = queryFactory.selectFrom(purchase)
            .join(purchase.purchaseProducts, purchaseProduct).fetchJoin()
            .where(purchase.accountId.eq(accountId))
            .where(builder)
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())

        val content = pagedQuery.fetch()

        return PageImpl(content, pageable, totalCount)
    }
}
