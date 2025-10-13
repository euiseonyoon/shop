package com.example.shop.purchase.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import java.time.OffsetDateTime

@Entity
class Payment(
    @Column(unique = true)
    val purchaseId: Long,

    @Column(unique = true)
    val paymentKey: String,
) {
    @Id @GeneratedValue
    val id: Long = 0

    @Column
    val createdAt: OffsetDateTime = OffsetDateTime.now()
}
