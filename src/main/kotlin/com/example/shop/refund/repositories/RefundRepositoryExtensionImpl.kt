package com.example.shop.refund.repositories

import com.example.shop.purchase.domain.QPurchase
import com.example.shop.refund.domain.QRefund
import com.example.shop.refund.domain.Refund
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
class RefundRepositoryExtensionImpl : QuerydslRepositorySupport(Refund::class.java), RefundRepositoryExtension {
    override fun searchByPurchaseId(purchaseId: Long): Refund? {
        val refund = QRefund.refund
        val purchase = QPurchase.purchase

        return from(refund)
            .where(refund.purchase.id.eq(purchaseId))
            .join(refund.purchase, purchase).fetchJoin()
            .fetchOne()
    }
}
