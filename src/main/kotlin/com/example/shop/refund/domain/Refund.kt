package com.example.shop.refund.domain

import com.example.shop.purchase.domain.Purchase
import com.example.shop.refund.enums.RefundStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import java.time.OffsetDateTime

@Entity
class Refund {
    @Id
    @GeneratedValue
    val id: Long? = null

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_id", nullable = false, unique = true)
    var purchase: Purchase? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: RefundStatus = RefundStatus.REQUESTED

    var reason: String? = null

    @Column(nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now()

    val updatedAt: OffsetDateTime? = null

    // 만약 환불거절될떄나 특정 내용을 남겨야 할 때 사용.
    var etc: String? = null
}
