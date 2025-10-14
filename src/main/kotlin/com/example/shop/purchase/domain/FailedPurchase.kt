package com.example.shop.purchase.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import java.time.OffsetDateTime

@Entity
class FailedPurchase(
    @Column
    val purchaseId: Long,
    @Column
    val errorCode: String,
    @Column
    val detailReason: String,
) {
    @Id @GeneratedValue
    val id: Long = 0

    @Column
    val createdAt: OffsetDateTime = OffsetDateTime.now()
}
